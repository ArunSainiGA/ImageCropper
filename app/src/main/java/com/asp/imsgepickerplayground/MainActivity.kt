package com.asp.imsgepickerplayground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.asp.imsgepickerplayground.ui.main.MainFragment
import com.asp.imsgepickerplayground.ui.main.NewApproachFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, NewApproachFragment.newInstance())
                .commitNow()
        }
    }
}