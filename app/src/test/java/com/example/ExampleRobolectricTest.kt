package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.editor.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// Robolectric's Android 16 (API 36) SDK provider currently fails in this CI environment.
// API 35 is sufficient for these resource and pure Kotlin highlighting tests.
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Repo Manager Pro", appName)
    }

    @Test
    fun `verify syntax highlighter identifies keywords and functions`() {
        val kotlinCode = """
            package com.example
            class Engine {
                fun start() {
                    val count = 42
                }
            }
        """.trimIndent()

        val highlighted = SyntaxHighlighter.highlight(kotlinCode, "kt", "Engine")
        assertTrue(highlighted.text.contains("fun start()"))
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }
}
