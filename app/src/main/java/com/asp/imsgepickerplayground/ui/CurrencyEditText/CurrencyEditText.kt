package com.asp.imsgepickerplayground.ui.CurrencyEditText

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import java.lang.ref.WeakReference

class CurrencyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {
    private val prefix = "$ "
    private val maxLength = 50


}

internal class CurrencyTextWatcher: TextWatcher {
    private var previousString: String = ""
    private var prefix: String = ""
    private var editText: WeakReference<EditText>? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        TODO("Not yet implemented")
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        TODO("Not yet implemented")
    }

    override fun afterTextChanged(s: Editable?) {
        val content = s.toString()

        // Is prefix there?
        if(content.length < prefix.length) {
            // No
            editText?.get()?.setText(prefix)
            editText?.get()?.setSelection(prefix.length)
            return
        }

        if(content.length == prefix.length) {
            // Yes
            return
        }

        val cleanContent = content.replace(prefix, "").replace("[,]".toRegex(), "")

        if(cleanContent == previousString || cleanContent.isEmpty()) {
            // Nothing to change here
            return
        }

        previousString = cleanContent

        
    }

}