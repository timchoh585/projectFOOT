package foot.project.projectfoot.Util

import android.content.Context
import foot.project.projectfoot.R

class SharedPrefs( private val context: Context ) {



    val sharedPref = context?.getSharedPreferences( "PROJECTFOOT", Context.MODE_PRIVATE)




    fun storeAverage( avg: DoubleArray ) {

    }



    fun storeTimes( time: Double ) {

    }
}
