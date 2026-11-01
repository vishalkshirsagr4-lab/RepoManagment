package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubUser(
    val login: String = "",
    val id: Long = 0,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    val name: String? = null,
    val bio: String? = null,
    @Json(name = "public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    val id: Long = 0,
    val name: String = "",
    @Json(name = "full_name") val fullName: String = "",
    val owner: GitHubUser = GitHubUser(),
    val private: Boolean = false,
    val description: String? = null,
    val fork: Boolean = false,
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "default_branch") val defaultBranch: String = "main",
    val language: String? = null,
    @Json(name = "stargazers_count") val stargazersCount: Int = 0,
    @Json(name = "forks_count") val forksCount: Int = 0,
    @Json(name = "open_issues_count") val openIssuesCount: Int = 0,
    @Json(name = "updated_at") val updatedAt: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubContentItem(
    val name: String = "",
    val path: String = "",
    val sha: String = "",
    val size: Long = 0,
    val url: String = "",
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "git_url") val gitUrl: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null,
    val type: String = "file", // "file", "dir", "symlink"
    val content: String? = null, // base64 encoded when fetching single file
    val encoding: String? = null
)

@JsonClass(generateAdapter = true)
data class GitCommitAuthor(
    val name: String = "",
    val email: String = "",
    val date: String = ""
)

@JsonClass(generateAdapter = true)
data class GitCommitDetails(
    val message: String = "",
    val author: GitCommitAuthor = GitCommitAuthor(),
    val committer: GitCommitAuthor = GitCommitAuthor()
)

@JsonClass(generateAdapter = true)
data class GitHubCommit(
    val sha: String = "",
    val commit: GitCommitDetails = GitCommitDetails(),
    val author: GitHubUser? = null,
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class BranchCommitInfo(
    val sha: String = "",
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubBranch(
    val name: String = "",
    val commit: BranchCommitInfo = BranchCommitInfo(),
    val protected: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CreateRepoRequest(
    val name: String,
    val description: String? = null,
    val private: Boolean = false,
    @Json(name = "auto_init") val autoInit: Boolean = true
)

@JsonClass(generateAdapter = true)
data class CommitFileRequest(
    val message: String,
    val content: String, // base64 encoded
    val sha: String? = null, // required if updating existing file
    val branch: String? = null
)

@JsonClass(generateAdapter = true)
data class CommitFileResponse(
    val content: GitHubContentItem? = null,
    val commit: GitHubCommit? = null
)

// GitHub Actions Workflow Models
@JsonClass(generateAdapter = true)
data class GitHubWorkflowRun(
    val id: Long = 0,
    val name: String = "",
    @Json(name = "head_branch") val headBranch: String? = "main",
    @Json(name = "head_sha") val headSha: String? = "",
    val status: String = "queued", // "queued", "in_progress", "completed"
    val conclusion: String? = null, // "success", "failure", "cancelled", etc.
    val event: String = "push",
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = "",
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "run_number") val runNumber: Int = 1
)

@JsonClass(generateAdapter = true)
data class WorkflowRunsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    @Json(name = "workflow_runs") val workflowRuns: List<GitHubWorkflowRun> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowJobStep(
    val name: String = "",
    val status: String = "queued", // "queued", "in_progress", "completed"
    val conclusion: String? = null,
    val number: Int = 1,
    @Json(name = "started_at") val startedAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class WorkflowJob(
    val id: Long = 0,
    @Json(name = "run_id") val runId: Long = 0,
    val name: String = "",
    val status: String = "queued",
    val conclusion: String? = null,
    @Json(name = "started_at") val startedAt: String = "",
    @Json(name = "completed_at") val completedAt: String? = null,
    val steps: List<WorkflowJobStep> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowJobsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    val jobs: List<WorkflowJob> = emptyList()
)

// GitHub Compare / Diff Models
@JsonClass(generateAdapter = true)
data class GitHubDiffFile(
    val sha: String = "",
    val filename: String = "",
    val status: String = "modified", // "added", "removed", "modified", "renamed"
    val additions: Int = 0,
    val deletions: Int = 0,
    val changes: Int = 0,
    val patch: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCompareResponse(
    val status: String = "identical",
    @Json(name = "ahead_by") val aheadBy: Int = 0,
    @Json(name = "behind_by") val behindBy: Int = 0,
    @Json(name = "total_commits") val totalCommits: Int = 0,
    val commits: List<GitHubCommit> = emptyList(),
    val files: List<GitHubDiffFile> = emptyList()
)

data class PushResult(
    val success: Boolean,
    val commitSha: String?,
    val branch: String,
    val message: String,
    val timestamp: String,
    val repoFullName: String,
    val filePath: String,
    val error: String? = null
)

// Repository Action Secrets Models
@JsonClass(generateAdapter = true)
data class GitHubSecret(
    val name: String = "",
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubSecretsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    val secrets: List<GitHubSecret> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubPublicKey(
    @Json(name = "key_id") val keyId: String = "",
    val key: String = ""
)

@JsonClass(generateAdapter = true)
data class CreateSecretBody(
    @Json(name = "encrypted_value") val encryptedValue: String,
    @Json(name = "key_id") val keyId: String
)

data class RepoSecretItem(
    val name: String,
    val createdAt: String = "",
    val updatedAt: String = "",
    val isLocal: Boolean = false
)

// GitHub Workflows & Dispatches
@JsonClass(generateAdapter = true)
data class GitHubWorkflow(
    val id: Long = 0,
    val name: String = "",
    val path: String = "",
    val state: String = "active", // "active", "disabled_manually", "disabled_inactivity"
    @Json(name = "badge_url") val badgeUrl: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class WorkflowsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    val workflows: List<GitHubWorkflow> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowDispatchRequest(
    val ref: String = "main",
    val inputs: Map<String, String>? = null
)

// Check Runs Models
@JsonClass(generateAdapter = true)
data class GitHubCheckRun(
    val id: Long = 0,
    val name: String = "",
    @Json(name = "head_sha") val headSha: String = "",
    val status: String = "queued", // "queued", "in_progress", "completed"
    val conclusion: String? = null, // "success", "failure", "neutral", "cancelled", etc.
    @Json(name = "started_at") val startedAt: String? = null,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "details_url") val detailsUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CheckRunsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    @Json(name = "check_runs") val checkRuns: List<GitHubCheckRun> = emptyList()
)

// Git Database API Models (for Atomic Batch Commit & Push)
@JsonClass(generateAdapter = true)
data class CreateGitBlobRequest(
    val content: String,
    val encoding: String = "base64" // "utf-8" or "base64"
)

@JsonClass(generateAdapter = true)
data class CreateGitBlobResponse(
    val sha: String = "",
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class GitTreeItem(
    val path: String,
    val mode: String = "100644", // "100644" normal file, "100755" executable
    val type: String = "blob", // "blob", "tree", "commit"
    val sha: String? = null // null for deleted file
)

@JsonClass(generateAdapter = true)
data class CreateGitTreeRequest(
    @Json(name = "base_tree") val baseTree: String? = null,
    val tree: List<GitTreeItem>
)

@JsonClass(generateAdapter = true)
data class GitTreeResponse(
    val sha: String = "",
    val url: String = "",
    val truncated: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GitRefObject(
    val sha: String = "",
    val type: String = "",
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class GitRefResponse(
    val ref: String = "",
    @Json(name = "node_id") val nodeId: String? = null,
    val url: String = "",
    val `object`: GitRefObject = GitRefObject()
)

@JsonClass(generateAdapter = true)
data class GitCommitTree(
    val sha: String = "",
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class GitCommitObjectResponse(
    val sha: String = "",
    val url: String = "",
    val tree: GitCommitTree = GitCommitTree(),
    val message: String = ""
)

@JsonClass(generateAdapter = true)
data class CreateGitCommitRequest(
    val message: String,
    val tree: String,
    val parents: List<String>
)

@JsonClass(generateAdapter = true)
data class UpdateGitRefRequest(
    val sha: String,
    val force: Boolean = false
)

// Staged Changes Models
enum class StagedChangeType {
    MODIFIED,
    CREATED,
    UPLOADED,
    DELETED
}

data class StagedChange(
    val repoFullName: String,
    val filePath: String,
    val branch: String = "main",
    val type: StagedChangeType,
    val textContent: String? = null,
    val base64Content: String? = null,
    val isBinary: Boolean = false,
    val originalSha: String? = null,
    val fileSizeFormatted: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


