# GitHub Container Registry Authentication Setup

**Epic**: CI/CD & Deployment Pipeline
**Status**: Ready for Dev
**Estimate**: XS
**Created Date**: 2026-01-06

## 1. User Story
**As a** developer
**I want** Watchtower to authenticate with GitHub Container Registry
**So that** it can pull private images automatically when new versions are pushed

## 2. Business Context & Value
GitHub Container Registry (ghcr.io) requires authentication to pull images from private repositories. The scoped Watchtower instance on the home server needs credentials to detect and pull new image versions. This is a one-time configuration task that enables the automated deployment pipeline.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: GitHub PAT is created with correct scopes
    *   Given you are logged into GitHub
    *   When you create a Personal Access Token (classic)
    *   Then it has at minimum the `read:packages` scope
    *   And it has an appropriate expiration (or no expiration for long-lived automation)

*   [ ] **Scenario 2**: Docker is authenticated on home server
    *   Given the PAT is created
    *   When `docker login ghcr.io -u <username>` is run on the NUC
    *   Then authentication succeeds
    *   And credentials are stored in `~/.docker/config.json`

*   [ ] **Scenario 3**: Watchtower can pull images
    *   Given Docker is authenticated with GHCR
    *   When Watchtower checks for new images
    *   Then it can successfully pull from `ghcr.io/jordijaspers/*`
    *   And no authentication errors appear in Watchtower logs

*   [ ] **Scenario 4**: Images are visible in GitHub Packages
    *   Given images are pushed by CI
    *   When viewing the repository's Packages tab on GitHub
    *   Then `eventify-server` and `eventify-client` packages are visible
    *   And they show the correct tags (`latest-dev`, `dev-<sha>`)

## 4. Technical Requirements

### GitHub Personal Access Token (PAT)
*   **Type**: Personal Access Token (classic) - NOT fine-grained
*   **Required scopes**:
    *   `read:packages` - Pull images from GHCR
    *   `write:packages` - Already handled by `GITHUB_TOKEN` in Actions (not needed for PAT)
*   **Expiration**: 90 days minimum, or "No expiration" for automation
*   **Name suggestion**: `eventify-watchtower-ghcr`

### Home Server Configuration

#### Option A: Docker Config File (Recommended)
```bash
# On the NUC, run:
echo "<PAT>" | docker login ghcr.io -u jordijaspers --password-stdin

# Verify:
cat ~/.docker/config.json
# Should show ghcr.io in auths
```

Then mount the config in Watchtower:
```yaml
watchtower:
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
    - ~/.docker/config.json:/config.json:ro
```

#### Option B: Environment Variables
```yaml
watchtower:
  environment:
    - REPO_USER=jordijaspers
    - REPO_PASS=<github_pat>
```
Note: This is less secure as the token is visible in docker-compose.yml

### Package Visibility (Optional)
If you want images to be public (no auth needed):
1.  Go to GitHub → Your Profile → Packages
2.  Select the package → Package Settings
3.  Change visibility to "Public"

However, keeping them private is more secure for a personal project.

### Security Considerations
*   PAT should have **minimal scopes** (only `read:packages`)
*   Store PAT securely (not in version control)
*   Consider using a dedicated GitHub "bot" account for automation (optional)
*   Rotate the PAT periodically (set a calendar reminder)

## 5. Design & UI/UX
*   N/A - Infrastructure only

## 6. Implementation Notes / Research

### Current Watchtower Config (from research)
The NUC's main Watchtower already has `REPO_USER` and `REPO_PASS` environment variables defined:
```yaml
environment:
  REPO_USER: ${REPO_USER}
  REPO_PASS: ${REPO_PASS}
```

This suggests the infrastructure for registry auth already exists. You may just need to:
1.  Create the GitHub PAT
2.  Add values to `/opt/projects/server-config/nuc/.env`

### Existing .env.example (from research)
```env
REPO_USER=
REPO_PASS=
```
These can be populated with GitHub credentials.

### Step-by-Step Setup

1.  **Create GitHub PAT**:
    *   Go to: GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
    *   Click "Generate new token (classic)"
    *   Name: `eventify-watchtower-ghcr`
    *   Expiration: 90 days or "No expiration"
    *   Scopes: ✅ `read:packages`
    *   Click "Generate token"
    *   **Copy the token immediately** (you won't see it again)

2.  **Update NUC .env file**:
    ```bash
    # SSH to NUC
    ssh nuc
    
    # Edit the .env file
    nano /opt/projects/server-config/nuc/.env
    
    # Add:
    REPO_USER=jordijaspers
    REPO_PASS=ghp_xxxxxxxxxxxxxxxxxxxx
    ```

3.  **Also authenticate Docker directly** (for the scoped Watchtower):
    ```bash
    echo "ghp_xxxxxxxxxxxxxxxxxxxx" | docker login ghcr.io -u jordijaspers --password-stdin
    ```

4.  **Restart Watchtower**:
    ```bash
    cd /opt/projects/server-config/nuc
    docker-compose --profile management restart watchtower
    
    # Also restart eventify watchtower
    cd /opt/projects/server-config/nuc/applications/eventify-tst
    docker-compose restart watchtower
    ```

5.  **Verify**:
    ```bash
    docker logs eventify_watchtower -f
    # Should not show any authentication errors
    ```

### Testing Before Full Pipeline
Before pushing real images, you can test the auth:
```bash
# On NUC, try to pull manually:
docker pull ghcr.io/jordijaspers/eventify-server:latest-dev
# Should work after CI pushes the first image
```

### Dependencies
*   **Depends on**: None
*   **Blocks**: CICD-003 (Watchtower won't work without auth)

### References
*   [GitHub PAT documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
*   [GHCR authentication](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry)
*   [Watchtower private registries](https://containrrr.dev/watchtower/private-registries/)
