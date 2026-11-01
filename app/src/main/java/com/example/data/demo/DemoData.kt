package com.example.data.demo

import com.example.data.remote.models.BranchCommitInfo
import com.example.data.remote.models.DeploymentProvider
import com.example.data.remote.models.GitCommitAuthor
import com.example.data.remote.models.GitCommitDetails
import com.example.data.remote.models.GitHubBranch
import com.example.data.remote.models.GitHubCommit
import com.example.data.remote.models.GitHubContentItem
import com.example.data.remote.models.GitHubRepo
import com.example.data.remote.models.GitHubUser
import com.example.data.remote.models.UnifiedDeployment

object DemoData {

    val demoUser = GitHubUser(
        login = "octodev",
        id = 10293847,
        avatarUrl = "https://avatars.githubusercontent.com/u/9919?v=4",
        name = "Alex Vance (Demo Mode)",
        bio = "Full-stack mobile engineer building next-gen developer tools and cloud infrastructures.",
        publicRepos = 14,
        followers = 342,
        following = 89,
        htmlUrl = "https://github.com/octodev"
    )

    val demoRepos = listOf(
        GitHubRepo(
            id = 101,
            name = "repomanager-core",
            fullName = "octodev/repomanager-core",
            owner = demoUser,
            private = false,
            description = "High-performance Git repository indexing and workspace management engine written in Kotlin.",
            defaultBranch = "main",
            language = "Kotlin",
            stargazersCount = 842,
            forksCount = 112,
            openIssuesCount = 5,
            updatedAt = "2026-10-30T18:40:00Z"
        ),
        GitHubRepo(
            id = 102,
            name = "cloud-edge-api",
            fullName = "octodev/cloud-edge-api",
            owner = demoUser,
            private = true,
            description = "Cloudflare Workers edge API for high-speed cache validation and webhook processing.",
            defaultBranch = "production",
            language = "TypeScript",
            stargazersCount = 215,
            forksCount = 18,
            openIssuesCount = 2,
            updatedAt = "2026-10-29T14:15:00Z"
        ),
        GitHubRepo(
            id = 103,
            name = "llm-space-explorer",
            fullName = "octodev/llm-space-explorer",
            owner = demoUser,
            private = false,
            description = "Gradio interactive application hosted on Hugging Face Spaces for exploring embeddings.",
            defaultBranch = "main",
            language = "Python",
            stargazersCount = 530,
            forksCount = 64,
            openIssuesCount = 8,
            updatedAt = "2026-10-28T09:30:00Z"
        ),
        GitHubRepo(
            id = 104,
            name = "mobile-ui-kit",
            fullName = "octodev/mobile-ui-kit",
            owner = demoUser,
            private = false,
            description = "Material 3 developer console design system and customizable syntax code editor for Android.",
            defaultBranch = "develop",
            language = "Kotlin",
            stargazersCount = 1240,
            forksCount = 195,
            openIssuesCount = 14,
            updatedAt = "2026-10-27T22:10:00Z"
        ),
        GitHubRepo(
            id = 105,
            name = "infra-terraform-cloudflare",
            fullName = "octodev/infra-terraform-cloudflare",
            owner = demoUser,
            private = true,
            description = "Declarative infrastructure definitions for DNS records, SSL certificates, and Workers.",
            defaultBranch = "main",
            language = "HCL",
            stargazersCount = 92,
            forksCount = 8,
            openIssuesCount = 0,
            updatedAt = "2026-10-25T11:05:00Z"
        )
    )

    fun getDemoContents(path: String): List<GitHubContentItem> {
        val clean = path.trim('/').lowercase()
        return when (clean) {
            "", "root" -> listOf(
                GitHubContentItem(name = "src", path = "src", sha = "d1", type = "dir"),
                GitHubContentItem(name = "docs", path = "docs", sha = "d2", type = "dir"),
                GitHubContentItem(name = "build.gradle.kts", path = "build.gradle.kts", sha = "f1", size = 1420, type = "file"),
                GitHubContentItem(name = "README.md", path = "README.md", sha = "f2", size = 2840, type = "file"),
                GitHubContentItem(name = "settings.json", path = "settings.json", sha = "f3", size = 512, type = "file"),
                GitHubContentItem(name = "worker.ts", path = "worker.ts", sha = "f4", size = 1890, type = "file"),
                GitHubContentItem(name = "app.py", path = "app.py", sha = "f5", size = 2100, type = "file")
            )
            "src" -> listOf(
                GitHubContentItem(name = "main", path = "src/main", sha = "d3", type = "dir"),
                GitHubContentItem(name = "test", path = "src/test", sha = "d4", type = "dir")
            )
            "src/main" -> listOf(
                GitHubContentItem(name = "kotlin", path = "src/main/kotlin", sha = "d5", type = "dir"),
                GitHubContentItem(name = "resources", path = "src/main/resources", sha = "d6", type = "dir")
            )
            "src/main/kotlin" -> listOf(
                GitHubContentItem(name = "RepositoryManager.kt", path = "src/main/kotlin/RepositoryManager.kt", sha = "f6", size = 3200, type = "file"),
                GitHubContentItem(name = "GitEngine.kt", path = "src/main/kotlin/GitEngine.kt", sha = "f7", size = 4120, type = "file")
            )
            "docs" -> listOf(
                GitHubContentItem(name = "architecture.md", path = "docs/architecture.md", sha = "f8", size = 3500, type = "file"),
                GitHubContentItem(name = "api-reference.md", path = "docs/api-reference.md", sha = "f9", size = 4900, type = "file")
            )
            else -> listOf(
                GitHubContentItem(name = "index.ts", path = "$path/index.ts", sha = "f10", size = 950, type = "file")
            )
        }
    }

    fun getDemoFileCode(path: String): String {
        return when {
            path.endsWith("RepositoryManager.kt") -> """
package com.repomanager.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Manages active cloud repository synchronization and caching.
 */
class RepositoryManager(
    private val client: CloudApiClient
) {
    private val _syncState = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncState: Flow<SyncStatus> = _syncState

    suspend fun synchronize(repoId: String): Result<Boolean> {
        _syncState.value = SyncStatus.Synchronizing(repoId)
        return try {
            val remoteCommits = client.fetchRecentCommits(repoId)
            applyLocalPatches(remoteCommits)
            _syncState.value = SyncStatus.Completed(repoId)
            Result.success(true)
        } catch (e: Exception) {
            _syncState.value = SyncStatus.Failed(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    private fun applyLocalPatches(commits: List<CommitData>) {
        // Fast-forward local workspace tree
    }
}
""".trimIndent()

            path.endsWith("app.py") -> """
import gradio as gr
import os

def analyze_repository(repo_url: str, depth: int):
    # Perform static AST analysis on repository structure
    status = f"Analyzing repository {repo_url} at depth {depth}"
    metrics = {
        "complexity_score": 14.8,
        "clean_architecture": True,
        "lines_of_code": 18450
    }
    return status, metrics

demo = gr.Interface(
    fn=analyze_repository,
    inputs=[gr.Textbox(label="Repository URL"), gr.Slider(1, 10, value=3)],
    outputs=[gr.Text(label="Status"), gr.JSON(label="Repository Metrics")],
    title="Hugging Face Space: Repo Explorer Pro"
)

if __name__ == "__main__":
    demo.launch(server_name="0.0.0.0", server_port=7860)
""".trimIndent()

            path.endsWith("worker.ts") -> """
export interface Env {
  GITHUB_WEBHOOK_SECRET: string;
  CACHE_STORE: KVNamespace;
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    if (url.pathname === "/health") {
      return new Response(JSON.stringify({ status: "healthy", timestamp: Date.now() }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    if (request.method === "POST" && url.pathname === "/api/webhook") {
      const payload = await request.json();
      console.log("Received GitHub deployment push:", payload);
      return new Response(JSON.stringify({ received: true }), { status: 200 });
    }

    return new Response("Repo Manager Pro Edge Gateway", { status: 200 });
  }
};
""".trimIndent()

            path.endsWith("README.md") -> """
# Repo Manager Pro

A native, high-performance developer workspace and cloud repository dashboard for Android.

## Features
- **Multi-Cloud Integration**: GitHub, Cloudflare Workers, and Hugging Face Spaces.
- **On-Device Code Editor**: Syntax highlighting, find-in-file, line numbering, and instant commits.
- **Repository Archiving**: One-tap full ZIP archive downloads saved via Android Storage.
- **AES KeyStore Encryption**: Never stores raw secrets or tokens in plain text.
- **Zero-Crash Offline Mode**: Local Room database caching for instant access anywhere.

## Architecture
- **Language**: Kotlin 2.2
- **UI**: Jetpack Compose + Material 3
- **Local Persistence**: Room SQLite
- **Networking**: Retrofit 2 + OkHttp + Moshi
""".trimIndent()

            path.endsWith("settings.json") -> """
{
  "editor.tabSize": 4,
  "editor.wordWrap": "off",
  "editor.formatOnSave": true,
  "git.autofetch": true,
  "security.workspace.trust.enabled": true
}
""".trimIndent()

            else -> """
// Source file: $path
// Opened in Repo Manager Pro Native Editor

fun main() {
    println("Hello from Repo Manager Pro!")
}
""".trimIndent()
        }
    }

    val demoCommits = listOf(
        GitHubCommit(
            sha = "7f8b91a",
            commit = GitCommitDetails(
                message = "feat(editor): optimize syntax token parsing and line gutter alignment",
                author = GitCommitAuthor(name = "Alex Vance", email = "alex@octodev.io", date = "2 hours ago")
            )
        ),
        GitHubCommit(
            sha = "3c2e14d",
            commit = GitCommitDetails(
                message = "fix(sync): resolve edge-case race condition during background zip archiving",
                author = GitCommitAuthor(name = "Alex Vance", email = "alex@octodev.io", date = "Yesterday")
            )
        ),
        GitHubCommit(
            sha = "9e51c82",
            commit = GitCommitDetails(
                message = "refactor: migrate repository cache to Room DAOs with reactive Flow streams",
                author = GitCommitAuthor(name = "Alex Vance", email = "alex@octodev.io", date = "3 days ago")
            )
        ),
        GitHubCommit(
            sha = "1a8f902",
            commit = GitCommitDetails(
                message = "chore(deps): update Android Gradle Plugin and Jetpack Compose BOM",
                author = GitCommitAuthor(name = "OctoBot", email = "bot@octodev.io", date = "5 days ago")
            )
        )
    )

    val demoBranches = listOf(
        GitHubBranch(name = "main", protected = true),
        GitHubBranch(name = "develop", protected = false),
        GitHubBranch(name = "feature/editor-syntax", protected = false),
        GitHubBranch(name = "release/v1.2", protected = true)
    )

    val demoDeployments = listOf(
        UnifiedDeployment(
            id = "dep-101",
            provider = DeploymentProvider.CLOUDFLARE,
            projectName = "cloud-edge-api",
            status = "SUCCESS",
            branch = "production",
            commitSha = "c89b21a",
            commitMessage = "deploy: release worker edge caching policy v2",
            deployedUrl = "https://cloud-edge-api.octodev.workers.dev",
            updatedAt = "15m ago",
            logsExcerpt = "[Cloudflare Edge] Worker script uploaded and routed to 280+ global PoPs in 1.4s."
        ),
        UnifiedDeployment(
            id = "dep-102",
            provider = DeploymentProvider.HUGGING_FACE,
            projectName = "llm-space-explorer",
            status = "RUNNING",
            branch = "main",
            commitSha = "4f91e80",
            commitMessage = "update: upgrade gradio frontend to 4.28 with live graphs",
            deployedUrl = "https://huggingface.co/spaces/octodev/llm-space-explorer",
            updatedAt = "1 hour ago",
            logsExcerpt = "[HF Spaces] Container healthy. Gradio server running on port 7860."
        ),
        UnifiedDeployment(
            id = "dep-103",
            provider = DeploymentProvider.GITHUB_ACTIONS,
            projectName = "repomanager-core",
            status = "SUCCESS",
            branch = "main",
            commitSha = "7f8b91a",
            commitMessage = "CI: run automated unit tests & Android lint checks",
            deployedUrl = "https://github.com/octodev/repomanager-core/actions/runs/849201",
            updatedAt = "2 hours ago",
            logsExcerpt = "[GitHub Actions] 42 tests passed. Build time: 1m 24s. Artifact uploaded."
        ),
        UnifiedDeployment(
            id = "dep-104",
            provider = DeploymentProvider.CLOUDFLARE,
            projectName = "mobile-docs-portal",
            status = "BUILDING",
            branch = "develop",
            commitSha = "3c2e14d",
            commitMessage = "docs: generate static API documentation bundle",
            deployedUrl = "https://mobile-docs.pages.dev",
            updatedAt = "Just now",
            logsExcerpt = "[Cloudflare Pages] Compiling static assets and bundling markdown..."
        )
    )
}
