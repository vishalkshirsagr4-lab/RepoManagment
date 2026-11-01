package com.example.ui.editor

enum class DiffType {
    EQUAL,
    INSERT,
    DELETE
}

data class DiffLine(
    val type: DiffType,
    val text: String,
    val oldLineNumber: Int?,
    val newLineNumber: Int?
)

data class DiffSummary(
    val additions: Int,
    val deletions: Int,
    val lines: List<DiffLine>
)

object DiffEngine {

    fun computeDiff(oldText: String, newText: String): DiffSummary {
        val oldLines = oldText.split("\n")
        val newLines = newText.split("\n")

        // Longest Common Subsequence (LCS) matrix
        val n = oldLines.size
        val m = newLines.size
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in 0 until n) {
            for (j in 0 until m) {
                if (oldLines[i] == newLines[j]) {
                    dp[i + 1][j + 1] = dp[i][j] + 1
                } else {
                    dp[i + 1][j + 1] = maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        // Backtrack to build diff
        val result = mutableListOf<DiffLine>()
        var i = n
        var j = m
        var additions = 0
        var deletions = 0

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldLines[i - 1] == newLines[j - 1]) {
                result.add(
                    DiffLine(
                        type = DiffType.EQUAL,
                        text = oldLines[i - 1],
                        oldLineNumber = i,
                        newLineNumber = j
                    )
                )
                i--
                j--
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                result.add(
                    DiffLine(
                        type = DiffType.INSERT,
                        text = newLines[j - 1],
                        oldLineNumber = null,
                        newLineNumber = j
                    )
                )
                additions++
                j--
            } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
                result.add(
                    DiffLine(
                        type = DiffType.DELETE,
                        text = oldLines[i - 1],
                        oldLineNumber = i,
                        newLineNumber = null
                    )
                )
                deletions++
                i--
            }
        }

        result.reverse()
        return DiffSummary(additions = additions, deletions = deletions, lines = result)
    }
}
