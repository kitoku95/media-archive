## Summary
<!-- Briefly describe which Phase or feature this PR implements -->
- **Phase / Branch**: 
- **Key Changes**:

## Type of Change
- [ ] `feat`: New feature
- [ ] `fix`: Bug fix
- [ ] `refactor`: Code refactoring
- [ ] `sec`: Security hardening
- [ ] `docs`: Documentation / Architecture update
- [ ] `test`: Unit / UI tests

## Hard Rules Compliance Checklist
- [ ] **[HR-01]** No automatic storage scanning (`MediaStore`) on startup or in the background.
- [ ] **[HR-02]** Default `Main` (`public`) Category is seeded and mandatory Category selection is enforced.
- [ ] **[HR-03]** `private` Categories accept any non-empty password (including 1 character/digit) and always require password authentication on access.
- [ ] **[HR-04]** Category detail screen displays 2 separate tabs (Images / Videos) in a 1:1 Square Grid.
- [ ] **[HR-05]** Full-screen Image Viewer supports horizontal swipe; Video Player supports Previous / Next controller buttons.
- [ ] **[HR-06]** Manual Storage Browsing (single files & folders) persists both `content_uri` and `thumbnail_path` in SQLite.
- [ ] **[HR-07]** Multi-theme palettes (Default Neutral Taupe + Deep Crimson / Forest / Sapphire), 3 languages (`en`, `vi`, `ja`) in Settings, and a concise `About` screen without technical jargon.
- [ ] **[HR-08]** Source code security standards met (salted password hash, UUID thumbnail filenames, `FLAG_SECURE` for `private` Categories, all `.md` documentation in English).

## Verification
- [ ] `./gradlew assembleDebug` succeeds without errors.
- [ ] `./gradlew testDebugUnitTest` passes all unit tests.
- [ ] `./gradlew lintDebug` reports no critical issues.
