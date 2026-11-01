package com.example.data.remote.api

import com.example.data.remote.models.CheckRunsResponse
import com.example.data.remote.models.CommitFileRequest
import com.example.data.remote.models.CommitFileResponse
import com.example.data.remote.models.CreateRepoRequest
import com.example.data.remote.models.CreateSecretBody
import com.example.data.remote.models.GitHubBranch
import com.example.data.remote.models.GitHubCommit
import com.example.data.remote.models.GitHubCompareResponse
import com.example.data.remote.models.GitHubContentItem
import com.example.data.remote.models.GitHubPublicKey
import com.example.data.remote.models.GitHubRepo
import com.example.data.remote.models.GitHubSecretsResponse
import com.example.data.remote.models.GitHubUser
import com.example.data.remote.models.GitHubWorkflow
import com.example.data.remote.models.WorkflowDispatchRequest
import com.example.data.remote.models.WorkflowJobsResponse
import com.example.data.remote.models.WorkflowRunsResponse
import com.example.data.remote.models.WorkflowsResponse
import com.example.data.remote.models.CreateGitBlobRequest
import com.example.data.remote.models.CreateGitBlobResponse
import com.example.data.remote.models.CreateGitCommitRequest
import com.example.data.remote.models.CreateGitTreeRequest
import com.example.data.remote.models.GitCommitObjectResponse
import com.example.data.remote.models.GitRefResponse
import com.example.data.remote.models.GitTreeResponse
import com.example.data.remote.models.UpdateGitRefRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface GitHubApiService {

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") auth: String
    ): GitHubUser

    @GET("user/repos")
    suspend fun getUserRepos(
        @Header("Authorization") auth: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,
        @Query("sort") sort: String = "updated",
        @Query("affiliation") affiliation: String = "owner,collaborator,organization_member"
    ): List<GitHubRepo>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepo(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRepo

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getDirectoryContents(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String = "",
        @Query("ref") ref: String? = null
    ): List<GitHubContentItem>

    @GET("repos/{owner}/{repo}/contents")
    suspend fun getRootContents(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("ref") ref: String? = null
    ): List<GitHubContentItem>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("ref") ref: String? = null
    ): GitHubContentItem

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Body body: CommitFileRequest
    ): CommitFileResponse

    @DELETE("repos/{owner}/{repo}/contents/{path}")
    suspend fun deleteFile(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("message") message: String,
        @Query("sha") sha: String,
        @Query("branch") branch: String? = null
    ): Response<Unit>

    @POST("user/repos")
    suspend fun createRepo(
        @Header("Authorization") auth: String,
        @Body body: CreateRepoRequest
    ): GitHubRepo

    @GET("repos/{owner}/{repo}/commits")
    suspend fun getCommits(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("sha") branch: String? = null,
        @Query("per_page") perPage: Int = 30
    ): List<GitHubCommit>

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<GitHubBranch>

    // Real-Time GitHub Actions Deployment & Workflows
    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getWorkflowRuns(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 20
    ): WorkflowRunsResponse

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/jobs")
    suspend fun getWorkflowJobs(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): WorkflowJobsResponse

    @POST("repos/{owner}/{repo}/actions/runs/{run_id}/rerun")
    suspend fun rerunWorkflow(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): Response<Unit>

    @POST("repos/{owner}/{repo}/actions/runs/{run_id}/cancel")
    suspend fun cancelWorkflowRun(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long
    ): Response<Unit>

    // Repository Workflows
    @GET("repos/{owner}/{repo}/actions/workflows")
    suspend fun getWorkflows(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): WorkflowsResponse

    @POST("repos/{owner}/{repo}/actions/workflows/{workflow_id}/dispatches")
    suspend fun triggerWorkflowDispatch(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow_id") workflowId: Long,
        @Body body: WorkflowDispatchRequest
    ): Response<Unit>

    @PUT("repos/{owner}/{repo}/actions/workflows/{workflow_id}/enable")
    suspend fun enableWorkflow(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow_id") workflowId: Long
    ): Response<Unit>

    @PUT("repos/{owner}/{repo}/actions/workflows/{workflow_id}/disable")
    suspend fun disableWorkflow(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow_id") workflowId: Long
    ): Response<Unit>

    // Check Runs
    @GET("repos/{owner}/{repo}/commits/{ref}/check-runs")
    suspend fun getCheckRunsForRef(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String
    ): CheckRunsResponse

    // Repository Secrets
@GET("repos/{owner}/{repo}/actions/secrets")
suspend fun getRepoSecrets(
    @Header("Authorization") auth: String,
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @retrofit2.http.Query("per_page") perPage: Int = 100,
    @retrofit2.http.Query("page") page: Int = 1
): GitHubSecretsResponse

    @GET("repos/{owner}/{repo}/actions/secrets/public-key")
    suspend fun getRepoPublicKey(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubPublicKey

    @PUT("repos/{owner}/{repo}/actions/secrets/{secret_name}")
    suspend fun createOrUpdateRepoSecret(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("secret_name") secretName: String,
        @Body body: CreateSecretBody
    ): Response<Unit>

    @DELETE("repos/{owner}/{repo}/actions/secrets/{secret_name}")
    suspend fun deleteRepoSecret(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("secret_name") secretName: String
    ): Response<Unit>

    // Real-Time Git Compare / Diff
    @GET("repos/{owner}/{repo}/compare/{basehead}")
    suspend fun compareCommits(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("basehead") basehead: String // e.g. "main...develop" or "sha1...sha2"
    ): GitHubCompareResponse

    @Streaming
    @GET("repos/{owner}/{repo}/zipball/{ref}")
    suspend fun downloadZipArchive(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String
    ): ResponseBody

    // Git Database REST API for atomic multi-file commit & push
    @POST("repos/{owner}/{repo}/git/blobs")
    suspend fun createGitBlob(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateGitBlobRequest
    ): CreateGitBlobResponse

    @GET("repos/{owner}/{repo}/git/ref/heads/{branch}")
    suspend fun getBranchRef(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String
    ): GitRefResponse

    @GET("repos/{owner}/{repo}/git/commits/{commit_sha}")
    suspend fun getGitCommit(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("commit_sha") commitSha: String
    ): GitCommitObjectResponse

    @POST("repos/{owner}/{repo}/git/trees")
    suspend fun createGitTree(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateGitTreeRequest
    ): GitTreeResponse

    @POST("repos/{owner}/{repo}/git/commits")
    suspend fun createGitCommit(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateGitCommitRequest
    ): GitCommitObjectResponse

    @PATCH("repos/{owner}/{repo}/git/refs/heads/{branch}")
    suspend fun updateBranchRef(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String,
        @Body body: UpdateGitRefRequest
    ): GitRefResponse
}

