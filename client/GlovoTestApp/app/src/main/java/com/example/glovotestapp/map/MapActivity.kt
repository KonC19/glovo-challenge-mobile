package com.example.glovotestapp.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.glovotestapp.Injection
import com.example.glovotestapp.R
import com.example.glovotestapp.data.City
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, MapContract.View {

    private val MY_LOCATION_REQUEST_CODE = 342
    private val CAMERA_ZOOM_DEFAULT = 13f
    private val CAMERA_ZOOM_THRESHOLD = 8f
    private val BOUNDS_PADDING = 100

    private lateinit var mMap: GoogleMap
    override lateinit var presenter: MapContract.Presenter

    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        presenter = MapPresenter(Injection.provideTasksRepository(), this, Injection.provideSchedulerProvider())

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
                presenter.loadMarkersForCities()
                setBottomSheet(false)
                showingMarkers = true
            } else {
                showingMarkers = false
            }
        }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
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
                    mMap.clear()
                    centerMapOnMyLocation(location.latitude, location.longitude)
                    presenter.checkLocation(location.latitude, location.longitude)
                } ?: presenter.loadCitiesForPicker()
            }
    }

    override fun showMapMarkers(citiesCoordinates: HashMap<City, LatLng>) {
        citiesCoordinates.forEach {
            val marker = mMap.addMarker(MarkerOptions().position(it.value))
            marker.tag = it.key
        }

        mMap.setOnMarkerClickListener {
            val city = it.tag as City
            mMap.clear()
            centerInCityAndDrawPolygons(city.workingArea)
            presenter.getCityDetails(city.code)
            true
        }
    }

    override fun showPolygonsForCity(city: City) {
        city.workingArea.forEach {
            val decoded = PolyUtil.decode(it)
            if (decoded.size != 0) {
                addPolygon(decoded)
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
            centerInCityAndDrawPolygons(it.workingArea)
            presenter.getCityDetails(it.code)
        }
        dialogFragment.show(ft, "dialogFragment")
    }

    override fun onCityDetailsLoaded(city: City) {
        bottom_sheet_header.text = city.name
        bottom_sheet_country_code.text = getString(R.string.country_code_text, city.countryCode)
        bottom_sheet_currency.text = getString(R.string.currency_text, city.currency)
        bottom_sheet_busy.text = getString(
            R.string.is_busy,
            if (city.busy) getString(R.string.is_busy_yes) else getString(R.string.is_busy_no))

        setBottomSheet(true)
    }

    private fun centerInCityAndDrawPolygons(workingArea: List<String>) {
        val decodedAll = mutableListOf<LatLng>()
        for (polyline in workingArea) {
            val decoded = PolyUtil.decode(polyline)
            decodedAll.addAll(decoded)
            if (decoded.size != 0) {
                addPolygon(decoded)
            }
        }
        val cu = CameraUpdateFactory.newLatLngBounds(getPolygonLatLngBounds(decodedAll), BOUNDS_PADDING)
        mMap.animateCamera(cu)
    }

    private fun addPolygon(decoded: List<LatLng>?) {
        val polygon = mMap.addPolygon(PolygonOptions().addAll(decoded))
        polygon.fillColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryTrans, null)
        polygon.strokeColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryTrans, null)
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

    private fun setBottomSheet(shouldShow: Boolean) {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        if (shouldShow) {
            bottomSheetBehavior.peekHeight = resources.getDimension(R.dimen.bottomsheet_peekheight).toInt()
        } else {
            bottomSheetBehavior.peekHeight = 0
        }
    }

    override fun showError(error: String?) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
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
