package com.example.domain

import kotlin.math.log2

enum class SecurityFramework(val displayName: String, val focusArea: String) {
    OWASP_TOP_10("OWASP Top 10 (2025/2026)", "Web Application Security Risks & Injection Defense"),
    NIST_CSF("NIST Cybersecurity Framework 2.0", "Identify, Protect, Detect, Respond, Recover"),
    CIS_BENCHMARKS("CIS Controls v8", "Asset Management, Account Security & Secure Configs"),
    ZERO_TRUST("Zero Trust Architecture (ZTA)", "Never Trust, Always Verify, Least Privilege Access")
}

data class PasswordStrength(
    val entropyBits: Double,
    val strengthLevel: String, // Weak, Moderate, Strong, Hardened
    val crackTimeEstimate: String,
    val recommendations: List<String>
)

class CyberSecurityEngine {

    /**
     * 1. Password Strength & Entropy Diagnostics
     */
    fun evaluateCredentialStrength(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(0.0, "Invalid", "Instant", listOf("Password cannot be empty."))
        }

        var poolSize = 0
        val recs = mutableListOf<String>()

        if (password.any { it.isLowerCase() }) poolSize += 26 else recs.add("Add lowercase letters.")
        if (password.any { it.isUpperCase() }) poolSize += 26 else recs.add("Add uppercase letters.")
        if (password.any { it.isDigit() }) poolSize += 10 else recs.add("Include numeric digits.")
        if (password.any { !it.isLetterOrDigit() }) poolSize += 32 else recs.add("Include special characters (@, #, $, etc.).")

        if (password.length < 12) recs.add("Increase length to at least 14-16 characters.")

        val entropy = password.length * log2(poolSize.toDouble().coerceAtLeast(1.0))

        val (level, crackTime) = when {
            entropy < 40 -> "Weak 🔴" to "< Few seconds"
            entropy < 65 -> "Moderate 🟡" to "A few hours to days"
            entropy < 85 -> "Strong 🟢" to "Decades to centuries"
            else -> "Hardened / Military Grade 🛡️" to "Trillions of years"
        }

        return PasswordStrength(entropy, level, crackTime, recs)
    }

    /**
     * 2. Hardening & Security Audit Framework Checklist
     */
    fun getFrameworkChecklist(framework: SecurityFramework): String {
        return when (framework) {
            SecurityFramework.OWASP_TOP_10 -> """
                🛡️ OWASP Top 10 Application Security Checklist:
                1. Broken Access Control: Enforce Role-Based Access Control (RBAC) and least privilege per object ID.
                2. Cryptographic Failures: Enforce TLS 1.3, AES-GCM-256, and argon2id / bcrypt for passwords.
                3. Injection (SQLi/XSS): Use parameterized queries / ORM prepared statements; sanitize HTML outputs.
                4. Insecure Design: Implement rate-limiting, account lockouts, and threat modeling pre-deployment.
                5. Security Misconfiguration: Disable default credentials, debug banners, and remove unused open ports.
                6. SSRF Protection: Restrict outgoing server requests to a strict whitelist of IP/host domains.
            """.trimIndent()

            SecurityFramework.NIST_CSF -> """
                🏛️ NIST CSF 2.0 Governance & Core Functions:
                • GOVERN (GV): Establish organizational context, cybersecurity strategy, and risk boundaries.
                • IDENTIFY (ID): Inventory digital assets, software dependencies (SBOM), and critical data stores.
                • PROTECT (PR): Multi-Factor Authentication (FIDO2 / TOTP), endpoint data encryption, awareness training.
                • DETECT (DE): Real-time SIEM logging, EDR telemetry, anomaly alerts, and behavioral baselines.
                • RESPOND (RS): Incident Response Plans (IRP), rapid isolation playbooks, and mitigation SLAs.
                • RECOVER (RC): Immutable air-gapped backups, disaster recovery testing, post-incident RCA.
            """.trimIndent()

            SecurityFramework.ZERO_TRUST -> """
                🔒 Zero Trust Architecture (ZTA) Principles:
                1. Continuous Verification: Always authenticate and authorize based on all available data points (user identity, device posture, location).
                2. Limit the Blast Radius: Micro-segment networks and minimize lateral movement with software-defined perimeters.
                3. Assume Breach: Encrypt data in transit and at rest; log and inspect every network transaction.
            """.trimIndent()

            SecurityFramework.CIS_BENCHMARKS -> """
                ⚙️ CIS Critical Security Controls (Highlights):
                • Control 01: Inventory & Control of Enterprise Assets
                • Control 03: Data Protection & Classification
                • Control 05: Account Management & Centralized Identity (IdP)
                • Control 10: Malware Defenses & Centralized Endpoint Management
            """.trimIndent()
        }
    }

    /**
     * 3. Static Security Audit for Vulnerable Code Snippets
     */
    fun auditCodeSecurity(snippet: String): String {
        val findings = mutableListOf<String>()

        if (snippet.contains("SELECT * FROM", ignoreCase = true) && snippet.contains("+")) {
            findings.add("🚨 Critical: Potential SQL Injection detected via string concatenation. Use parameterized queries.")
        }
        if (snippet.contains("password", ignoreCase = true) && (snippet.contains("\"") || snippet.contains("'"))) {
            findings.add("⚠️ Warning: Hardcoded credential or token detected. Use environment variables or Secret Vaults.")
        }
        if (snippet.contains("MD5", ignoreCase = true) || snippet.contains("SHA1", ignoreCase = true)) {
            findings.add("🚨 Critical: Weak hashing algorithm (MD5/SHA1). Upgrade to SHA-256, Argon2id, or PBKDF2.")
        }
        if (snippet.contains("Access-Control-Allow-Origin: *", ignoreCase = true)) {
            findings.add("⚠️ Notice: Wildcard CORS configuration allows arbitrary cross-origin data extraction.")
        }

        return if (findings.isEmpty()) {
            "✅ No immediate obvious vulnerabilities (SQLi, hardcoded tokens, weak crypto) found in the snippet."
        } else {
            "🛡️ Security Audit Findings:\n" + findings.joinToString("\n")
        }
    }
}
