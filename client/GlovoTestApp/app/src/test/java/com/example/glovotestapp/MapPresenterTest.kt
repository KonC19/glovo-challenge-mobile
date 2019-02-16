package com.example.glovotestapp

import com.example.glovotestapp.data.City
import com.example.glovotestapp.data.MapRepository
import com.example.glovotestapp.map.MapContract
import com.example.glovotestapp.map.MapPresenter
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MapPresenterTest {

    @Mock
    private lateinit var mapsRepository: MapRepository

    @Mock
    private lateinit var mapsView: MapContract.View

    private lateinit var mapsPresenter: MapPresenter

    private lateinit var cities: MutableList<City>

    @Before
    fun setupMapsPresenter() {
        MockitoAnnotations.initMocks(this)

        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler -> Schedulers.io() }

        mapsPresenter = MapPresenter(mapsRepository, mapsView)

        cities = mutableListOf(
            City("BIL", "ES", "Bilbao", "EUR", true, false, "CET", "es", listOf("k_agG`c}PgE_QnKmdAgNwXjIuHzUb^bB|ZkHtu@")),
            City("CRE", "ES", "Ciudad Real", "EUR", true, false, "CET", "es", listOf("ac~lFvr`W`JxcCtH|`AjhAnc@tc@qGpi@gJhr@sqAxIclCKocCuz@alCsf@jb@oP|i@wLPsHhw@j`@vg@|AhtAuh@ny@aImi@w[xJt@pUa]uW")),
            City("ALC", "Alicante", "Bilbao", "EUR", true, false, "CET", "es", listOf("u{}hFfsaBsDNaVkb@iDwc@zA]k@uXZeIzL{MpJdLd]dg@k@hM}NlRwArEe@`Ke@`KCbD"))
        )
    }

    @Test fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mapsPresenter = MapPresenter(mapsRepository, mapsView)

        // Then the presenter is set to the view
        verify(mapsView).presenter = mapsPresenter
    }

    @Test fun loadCitiesFromRepository_LoadIntoView() {
        `when`(mapsRepository.getCities()).thenReturn(Single.just(cities))
        mapsPresenter.loadCitiesForPicker()

        verify(mapsView).showCityPicker(cities)
    }

    @Test fun loadCityDetailsFromRepository_LoadIntoView() {
//
//        `when`(mapsRepository.getCityDetails("BIL")).thenReturn(Single.just(cities[0]))

    }
}
