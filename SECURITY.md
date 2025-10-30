# Security Scanning with OWASP Dependency-Check

This project uses [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/) to scan dependencies for known vulnerabilities.

## Quick Start

### Run Security Scan

```bash
# Scan dependencies for vulnerabilities
mvn dependency-check:check

# View HTML report
open target/dependency-check/dependency-check-report.html
```

## NVD API Key (Highly Recommended)

The National Vulnerability Database (NVD) requires an API key for reliable access. Without it, you may experience rate limiting or 403/404 errors.

### Get an API Key

1. **Request an API key:** https://nvd.nist.gov/developers/request-an-api-key
2. **Receive key via email** (usually within a few hours)
3. **Set environment variable:**

   ```bash
   # Linux/Mac
   export NVD_API_KEY="your-api-key-here"

   # Or add to ~/.bashrc or ~/.zshrc
   echo 'export NVD_API_KEY="your-api-key-here"' >> ~/.bashrc
   ```
4. **Enable in pom.xml** (uncomment the line):

   ```xml
   <configuration>
     <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
   </configuration>
   ```

### Without API Key

If you don't have an API key, the plugin will:
- ✅ Still scan your dependencies
- ⚠️ Use cached vulnerability data (may be outdated)
- ⚠️ May fail to download new vulnerability data

The plugin is configured with `<failOnError>false</failOnError>` to prevent build failures from NVD API issues.

## Configuration

### Current Settings

|       Setting        |                Value                |              Description               |
|----------------------|-------------------------------------|----------------------------------------|
| **failBuildOnCVSS**  | 8                                   | Fail build on High severity (CVSS ≥ 8) |
| **Formats**          | HTML, JSON                          | Report formats                         |
| **Output**           | `target/dependency-check/`          | Report location                        |
| **Suppression File** | `dependency-check-suppressions.xml` | False positive suppressions            |

### Severity Levels (CVSS)

|   CVSS Score   |   Severity   |     Action      |
|----------------|--------------|-----------------|
| 0.0 - 3.9      | Low          | Report only     |
| 4.0 - 6.9      | Medium       | Report only     |
| 7.0 - 7.9      | High         | Report only     |
| **8.0 - 10.0** | **Critical** | **BUILD FAILS** |

## Usage Examples

### 1. Run Scan

```bash
mvn dependency-check:check
```

### 2. Update Vulnerability Database Only

```bash
mvn dependency-check:update-only
```

### 3. Generate Report Without Build

```bash
mvn dependency-check:aggregate
```

### 4. Skip Security Scan

```bash
mvn clean install -Ddependency-check.skip=true
```

### 5. Run Scan in CI/CD

```bash
# With API key from environment
mvn dependency-check:check

# View report
cat target/dependency-check/dependency-check-report.json
```

## Suppressing False Positives

When a vulnerability is reported but doesn't apply to your usage:

1. **Verify the vulnerability** - Understand why it's reported
2. **Confirm it's a false positive** - Check if it affects your code
3. **Add suppression** - Edit `dependency-check-suppressions.xml`

### Example Suppression

```xml
<suppress>
  <notes><![CDATA[
    CVE-2024-1234 doesn't apply because:
    - We don't use the affected Jackson XML module
    - Vulnerability requires XML deserialization which we don't do
  ]]></notes>
  <packageUrl regex="true">^pkg:maven/com\.fasterxml\.jackson\.dataformat/jackson-dataformat-xml@.*$</packageUrl>
  <cve>CVE-2024-1234</cve>
</suppress>
```

## Integration with Pre-commit Hooks

You can add dependency-check to your pre-commit workflow by updating `.git/hooks/pre-commit`:

```bash
#!/bin/bash
echo "Running security scan..."
mvn dependency-check:check -q || {
  echo "❌ Security vulnerabilities found! Check target/dependency-check/dependency-check-report.html"
  exit 1
}
```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Run OWASP Dependency-Check
  env:
    NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
  run: mvn dependency-check:check

- name: Upload Security Report
  uses: actions/upload-artifact@v3
  with:
    name: dependency-check-report
    path: target/dependency-check/
```

### GitLab CI

```yaml
security-scan:
  script:
    - mvn dependency-check:check
  artifacts:
    paths:
      - target/dependency-check/
    when: always
  variables:
    NVD_API_KEY: $NVD_API_KEY
```

## Troubleshooting

### Issue: "NVD returned 403/404 error"

**Cause:** No NVD API key or rate limiting

**Solution:**
1. Get an NVD API key (see above)
2. Or run with cached data: `mvn dependency-check:check -Dnvd.enabled=false`

### Issue: Build takes too long (first run)

**Cause:** Downloading NVD database (~200MB)

**Solution:**
- First run takes 2-5 minutes to download vulnerability database
- Subsequent runs are much faster (30-60 seconds)
- Database is cached in `~/.m2/repository/org/owasp/dependency-check-data/`

### Issue: False positives

**Cause:** CPE matching isn't perfect

**Solution:**
- Add suppressions to `dependency-check-suppressions.xml`
- Document why the vulnerability doesn't apply

## Resources

- **Official Documentation:** https://jeremylong.github.io/DependencyCheck/
- **NVD API Key:** https://nvd.nist.gov/developers/request-an-api-key
- **Suppression Documentation:** https://jeremylong.github.io/DependencyCheck/general/suppression.html
- **Maven Plugin Docs:** https://jeremylong.github.io/DependencyCheck/dependency-check-maven/

---

*Last Updated: 2025-10-23*
