package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType
import com.example.ui.theme.SyntaxVariable
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val KOTLIN_KEYWORDS = setOf(
        "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return",
        "super", "this", "throw", "true", "try", "typealias", "val", "var", "when", "while",
        "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
        "get", "import", "init", "param", "property", "receiver", "set", "setparam",
        "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
        "data", "enum", "expect", "external", "final", "infix", "inline", "inner",
        "internal", "lateinit", "noinline", "open", "operator", "out", "override",
        "private", "protected", "public", "reified", "sealed", "suspend", "tailrec", "vararg"
    )

    private val PYTHON_KEYWORDS = setOf(
        "and", "as", "assert", "async", "await", "break", "class", "continue", "def",
        "del", "elif", "else", "except", "False", "finally", "for", "from", "global",
        "if", "import", "in", "is", "lambda", "None", "nonlocal", "not", "or",
        "pass", "raise", "return", "True", "try", "while", "with", "yield"
    )

    private val JS_TS_KEYWORDS = setOf(
        "break", "case", "catch", "class", "const", "continue", "debugger", "default",
        "delete", "do", "else", "export", "extends", "finally", "for", "function",
        "if", "import", "in", "instanceof", "new", "return", "super", "switch",
        "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield",
        "let", "static", "enum", "await", "async", "interface", "type", "implements",
        "package", "private", "protected", "public", "null", "true", "false", "undefined"
    )

    private val JAVA_KEYWORDS = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "true", "false", "null"
    )

    private val KEYWORD_PATTERN = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
    private val STRING_PATTERN = Pattern.compile("\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|`(\\\\.|[^`\\\\])*`")
    private val LINE_COMMENT_SLASH = Pattern.compile("//.*")
    private val LINE_COMMENT_HASH = Pattern.compile("#.*")
    private val BLOCK_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/")
    private val NUMBER_PATTERN = Pattern.compile("\\b(\\d+(\\.\\d+)?([eE][+-]?\\d+)?[fFdDlL]?|0x[0-9a-fA-F]+)\\b")
    private val FUNCTION_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()")

    fun highlight(
        code: String,
        language: String,
        searchQuery: String = ""
    ): AnnotatedString {
        val keywords = when (language.lowercase()) {
            "kt", "kotlin" -> KOTLIN_KEYWORDS
            "py", "python" -> PYTHON_KEYWORDS
            "js", "jsx", "ts", "tsx", "javascript", "typescript" -> JS_TS_KEYWORDS
            "java" -> JAVA_KEYWORDS
            "json" -> setOf("true", "false", "null")
            else -> KOTLIN_KEYWORDS + PYTHON_KEYWORDS + JS_TS_KEYWORDS
        }

        return buildAnnotatedString {
            append(code)

            // Base style: monospaced, light font
            addStyle(
                SpanStyle(
                    color = SyntaxVariable,
                    fontFamily = FontFamily.Monospace
                ),
                0,
                code.length
            )

            if (code.isEmpty()) return@buildAnnotatedString

            // 1. Highlight numbers
            val numMatcher = NUMBER_PATTERN.matcher(code)
            while (numMatcher.find()) {
                addStyle(SpanStyle(color = SyntaxNumber), numMatcher.start(), numMatcher.end())
            }

            // 2. Highlight functions
            val funcMatcher = FUNCTION_PATTERN.matcher(code)
            while (funcMatcher.find()) {
                val start = funcMatcher.start(1)
                val end = funcMatcher.end(1)
                if (start in 0..code.length && end in 0..code.length) {
                    addStyle(
                        SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Medium),
                        start,
                        end
                    )
                }
            }

            // 3. Highlight keywords & Capitalized Types
            val kwMatcher = KEYWORD_PATTERN.matcher(code)
            while (kwMatcher.find()) {
                val word = kwMatcher.group()
                val start = kwMatcher.start()
                val end = kwMatcher.end()
                if (keywords.contains(word)) {
                    addStyle(
                        SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold),
                        start,
                        end
                    )
                } else if (word.firstOrNull()?.isUpperCase() == true && word.length > 1) {
                    addStyle(SpanStyle(color = SyntaxType), start, end)
                }
            }

            // 4. Highlight strings
            val strMatcher = STRING_PATTERN.matcher(code)
            while (strMatcher.find()) {
                addStyle(SpanStyle(color = SyntaxString), strMatcher.start(), strMatcher.end())
            }

            // 5. Highlight comments (overrides keywords inside comments)
            val lineCommentPattern = if (language.equals("py", ignoreCase = true)) {
                LINE_COMMENT_HASH
            } else {
                LINE_COMMENT_SLASH
            }
            val lcMatcher = lineCommentPattern.matcher(code)
            while (lcMatcher.find()) {
                addStyle(SpanStyle(color = SyntaxComment), lcMatcher.start(), lcMatcher.end())
            }

            val bcMatcher = BLOCK_COMMENT.matcher(code)
            while (bcMatcher.find()) {
                addStyle(SpanStyle(color = SyntaxComment), bcMatcher.start(), bcMatcher.end())
            }

            // 6. Highlight search matches
            if (searchQuery.isNotBlank() && searchQuery.length >= 2) {
                val queryRegex = Regex.escape(searchQuery)
                val searchPattern = Pattern.compile(queryRegex, Pattern.CASE_INSENSITIVE)
                val sMatcher = searchPattern.matcher(code)
                while (sMatcher.find()) {
                    addStyle(
                        SpanStyle(
                            background = Color(0xFFFBBF24),
                            color = Color(0xFF090B12),
                            fontWeight = FontWeight.Bold
                        ),
                        sMatcher.start(),
                        sMatcher.end()
                    )
                }
            }
        }
    }
}
