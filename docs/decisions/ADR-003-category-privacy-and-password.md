# ADR-003: Category Access Control (`public` / `private`) & Unrestricted Password Policy

- **Status**: Approved
- **Date**: 2026-11-15
- **Related Rules**: HR-02, HR-03, HR-08

---

## 1. Context

**MediaArchive** organizes all media by Category:
- A default `Main` Category (`public` mode) is always available and cannot be deleted.
- Users can create custom Categories with a custom name and choose either `public` or `private` mode:
  - `public`: Tapping opens the Category immediately.
  - `private`: Creating the Category requires setting a password (**any non-empty password is accepted, including a single character or a single digit, with no format restrictions**). Tapping a `private` Category always prompts for the password before opening.

---

## 2. Decision

1. **Validation Rules on Category Creation**:
   - Category Name: Must be non-blank after trimming and unique (case-insensitive).
   - `PUBLIC` Mode: No password required (`password_hash = null`, `password_salt = null`).
   - `PRIVATE` Mode:
     - Only requires `password.isNotEmpty()` (`length >= 1`).
     - **Never enforce minimum length or character complexity**, allowing quick 1-digit or 1-character passwords as requested by the user.
2. **Cryptographic Storage in SQLite**:
   - Even for short passwords, hash the password with a unique 16-byte `SecureRandom` salt per Category using `PBKDF2WithHmacSHA256` (or salted `SHA-256`) before storing `password_hash` and `password_salt` in `categories`.
3. **Session-Scoped Unlock**:
   - Unlocking a `private` Category grants access only for the active navigation flow (`CategoryDetailScreen` $\rightarrow$ `ImageViewerScreen` / `VideoPlayerScreen`).
   - Returning to `CategoryListScreen` resets the unlock state so tapping the `private` Category again prompts for the password.

---

## 3. Consequences

### Positive:
- Matches the exact user requirement for frictionless custom passwords (1 character/digit allowed).
- Protects stored passwords against plain-text inspection in SQLite.
