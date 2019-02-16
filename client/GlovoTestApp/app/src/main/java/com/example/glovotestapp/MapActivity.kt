package com.example.glovotestapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.example.glovotestapp.data.City
import com.example.glovotestapp.data.Country
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_map.*
import android.location.Geocoder
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.res.ResourcesCompat
import java.util.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlin.collections.HashMap


class MapActivity : AppCompatActivity(), OnMapReadyCallback, MapContract.View {

    private val MY_LOCATION_REQUEST_CODE = 342
    private val CAMERA_ZOOM_DEFAULT = 14f
    private val CAMERA_ZOOM_THRESHOLD = 8f
    private val BOUNDS_PADDING = 300

    private lateinit var mMap: GoogleMap
    override lateinit var presenter: MapContract.Presenter

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var lastLocation : Location

    var showingMarkers : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        setSupportActionBar(toolbar)
        setBottomSheet(false)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        presenter = MapPresenter(Injection.provideTasksRepository(applicationContext), this)

    }

    private fun centerMapOnMyLocation(latitude: Double, longitude: Double) {
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latitude, longitude),
                CAMERA_ZOOM_DEFAULT
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnCameraIdleListener {
            if (mMap.cameraPosition.zoom < CAMERA_ZOOM_THRESHOLD && !showingMarkers) {
                mMap.clear()
                presenter.showMarkersForCities()
                setBottomSheet(false)
                showingMarkers = true
            } else {
                showingMarkers = false
            }
//            else if (mMap.cameraPosition.zoom > CAMERA_ZOOM_THRESHOLD && showingMarkers){
//                mMap.clear()
//                showingMarkers = false
//                presenter.checkLocation(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude)
//            }

//            presenter.checkLocation(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude)
        }

        checkLocationPermission()
    }

    fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_LOCATION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                location?.let {
                    lastLocation = it
                    mMap.clear()
                    centerMapOnMyLocation(location.latitude, location.longitude)
                    presenter.checkLocation(location.latitude, location.longitude)
                } ?: presenter.loadCitiesForPicker()
            }
    }

    private fun findLocationFromVisibleRegion() {
        val visibleRegion = mMap.projection.visibleRegion
        val latlng = visibleRegion.latLngBounds.center
//        getCityFromLatLng(latlng.latitude, latlng.longitude)
    }

    fun getCountryFromLatLng(lat: Double, lng: Double) : String? {
        val gcd = Geocoder(this, Locale.getDefault())
        val addresses = gcd.getFromLocation(lat, lng, 1)
        return if (addresses.size > 0) {
            addresses[0].countryCode
        } else {
            null
        }
    }

    override fun showMapMarkers(citiesCoordinates: HashMap<City, LatLng>) {
        citiesCoordinates.forEach {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(it.value)
            )
            marker.tag = it.key
        }

        mMap.setOnMarkerClickListener {
            val city = it.tag as City
            mMap.clear()
            centerInCityAndDrawPolygones(city.workingArea)
            presenter.getCityDetails(city.code)
            true
        }
    }

    override fun showPolygonsForCity(city: City) {
        city.workingArea.forEach {
            val decoded = PolyUtil.decode(it)
            if (decoded.size != 0) {
                val polygon = mMap.addPolygon(PolygonOptions().addAll(decoded))
                polygon.fillColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryTrans, null)
                polygon.strokeColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryTrans, null)
            }
        }
    }

    override fun showCityPicker(cities: List<City>) {
        val ft = supportFragmentManager.beginTransaction()
        val dialogFragment = CityPickerDialogFragment.newInstance(cities)
        dialogFragment.isCancelable = false
        dialogFragment.cityCallback = {
            dialogFragment.dismiss()
            mMap.clear()
            centerInCityAndDrawPolygones(it.workingArea)
            presenter.getCityDetails(it.code)
        }
        dialogFragment.show(ft, "dialogFragment")

//        val transaction = supportFragmentManager.beginTransaction()
//        val fragment = CityPickerDialogFragment.newInstance(cities)
//        transaction.add(android.R.id.content, fragment)
//        transaction.addToBackStack(null)
//        transaction.commit()
//
//        fragment.cityCallback = {
//            supportFragmentManager.popBackStack()
//            Toast.makeText(this, it.name, Toast.LENGTH_LONG).show()
//        }
    }

    fun centerInCityAndDrawPolygones(workingArea: List<String>) {
        var isCentered = false
        for (polyline in workingArea) {
            val decoded = PolyUtil.decode(polyline)
            if (decoded.size != 0) {
                if (!isCentered && decoded.size > 1) {
                    val cu = CameraUpdateFactory.newLatLngBounds(getPolygonLatLngBounds(decoded), BOUNDS_PADDING)
                    mMap.animateCamera(cu)
                    isCentered = true
                }
                val polygon = mMap.addPolygon(PolygonOptions().addAll(decoded))
                polygon.fillColor = 0x7F00FF00
                polygon.strokeColor = Color.BLUE
            }
        }
    }

    private fun getPolygonLatLngBounds(decoded: List<LatLng>): LatLngBounds? {
        val centerBuilder = LatLngBounds.builder()
        for (point in decoded) {
            centerBuilder.include(point)
        }
        return centerBuilder.build()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.size == 1 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                presenter.loadCitiesForPicker()
            }
        }

    }

    fun isLocationInWorkingArea(workingArea: List<String>, lat: Double, lng: Double) : Boolean {
        for (polyline in workingArea) {
            val decoded = PolyUtil.decode(polyline)
            if (decoded.size != 0) {
                if (PolyUtil.containsLocation(lat, lng, decoded, false)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onCountriesLoaded(countries: List<Country>) {

    }

    override fun onCitiesLoaded(cities: List<City>) {
//        var isLocationInWorkingAreaFound = false
//        cities.forEach {
//            if (isLocationInWorkingArea(it.workingArea, lastLocation.latitude, lastLocation.longitude)) {
//                for (polyline in it.workingArea) {
//                    val decoded = PolyUtil.decode(polyline)
//                    if (decoded.size != 0) {
//                        val polygon = mMap.addPolygon(PolygonOptions().addAll(decoded))
//                        polygon.fillColor = 0x7F00FF00
//                        polygon.strokeColor = Color.BLUE
//                    }
//                }
//                presenter.getCityDetails(it.code)
//                isLocationInWorkingAreaFound = true
//                return@forEach
//            }
//        }
//
//        if (!isLocationInWorkingAreaFound) presenter.loadCitiesForPicker()

//        val finalDecoded = mutableListOf<LatLng>()
//
//        for (polyline in cities[4].workingArea) {
//
//            if (polyline != "" ) {
//                val decoded = PolyUtil.decode(polyline)
//                decoded.forEach {
//                    if (!PolyUtil.containsLocation(it, finalDecoded, false)) {
//                        finalDecoded.add(it)
//                    } else {
//                        println("yoooooo")
//                    }
//                }
//            }
//        }
//
//        val polygon = mMap.addPolygon(PolygonOptions().addAll(finalDecoded))
//        polygon.fillColor = 0x7F00FF00
//        polygon.strokeColor = Color.BLUE

//        cities.forEach {
//            //            val decoded = mutableListOf<LatLng>()
////            PolyUtil.decode(it.workingArea)
//            for (polyline in it.workingArea) {
//                val decoded = PolyUtil.decode(polyline)
//                if (decoded.size != 0) {
//                    val polygon = mMap.addPolygon(PolygonOptions().addAll(decoded))
//                    polygon.fillColor = 0x7F00FF00
//                    polygon.strokeColor = Color.BLUE
//
////                    mMap.setLatLngBoundsForCameraTarget(getPolygonLatLngBounds(decoded))
//                }
//            }
//        }
    }

    override fun onCityDetailsLoaded(city: City) {
        bottom_sheet_header.text = city.name
        bottom_sheet_country_code.text = getString(R.string.country_code_text, city.countryCode)
        bottom_sheet_currency.text = getString(R.string.currency_text, city.currency)
        bottom_sheet_busy.text = getString(R.string.is_busy,
            if (city.busy) getString(R.string.is_busy_yes) else getString(R.string.is_busy_no))

        setBottomSheet(true)
    }

    fun setBottomSheet(shouldShow: Boolean) {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        if (shouldShow) {
            bottomSheetBehavior.peekHeight = resources.getDimension(R.dimen.bottomsheet_peekheight).toInt()
        } else {
            bottomSheetBehavior.peekHeight = 0
        }
    }

    public override fun onStart() {
        super.onStart()
        presenter.subscribe()
    }

    public override fun onStop() {
        super.onStop()
        presenter.unsubscribe()
    }
}
