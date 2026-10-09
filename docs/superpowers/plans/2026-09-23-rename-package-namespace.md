# Rename Package Namespace Root Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename the project's package namespace root from `net.cynreub` to `xyz.drreub` entirely through the project, including file contents and directory structures.

**Architecture:** We will create a robust python script to execute both the file-content string replacements and the disk-level directory restructuring, ensuring atomic and clean changes. Then we will verify via Gradle build and testing.

**Tech Stack:** Python 3 (standard scripting library), Gradle, Android SDK build environment.

---

### Task 1: Create Automated Namespace Renaming Script

**Files:**
- Create: `rename_namespace.py`

- [ ] **Step 1: Write the python script to perform the renaming and path movement**

Create `rename_namespace.py` at the project root with the following code:

```python
import os
import shutil

def replace_in_file(file_path, old_str, new_str):
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        if old_str in content:
            new_content = content.replace(old_str, new_str)
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Replaced in: {file_path}")
    except Exception as e:
        print(f"Error replacing in {file_path}: {e}")

def rename_package(root_dir, old_pkg, new_pkg):
    # Step 1: Replace text in all relevant source and config files
    for dirpath, _, filenames in os.walk(root_dir):
        # Skip build, .gradle, .git, and .idea directories
        if any(ignored in dirpath for ignored in ['build', '.gradle', '.git', '.idea']):
            continue
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            # Focus on source files, configurations, layouts, navigation, etc.
            if filename.endswith(('.java', '.kt', '.xml', '.gradle', '.properties', '.kts', '.txt', '.pro')):
                replace_in_file(file_path, old_pkg, new_pkg)
                
    # Step 2: Move directories on disk for Java/Kotlin source roots
    source_roots = [
        'app/src/main/java',
        'app/src/androidTest/java',
        'app/src/test/java'
    ]
    
    old_pkg_path = old_pkg.replace('.', '/')
    new_pkg_path = new_pkg.replace('.', '/')
    
    for src_root in source_roots:
        full_src_root = os.path.join(root_dir, src_root)
        if not os.path.exists(full_src_root):
            continue
            
        old_dir = os.path.join(full_src_root, old_pkg_path, "weighday")
        new_parent_dir = os.path.join(full_src_root, new_pkg_path)
        new_dir = os.path.join(new_parent_dir, "weighday")
        
        if os.path.exists(old_dir):
            print(f"Creating parent directory: {new_parent_dir}")
            os.makedirs(new_parent_dir, exist_ok=True)
            
            print(f"Moving {old_dir} -> {new_dir}")
            shutil.move(old_dir, new_dir)
            
            # Clean up empty parent directories left behind:
            # First, clean old_pkg_path (e.g., net/cynreub)
            empty_cynreub = os.path.join(full_src_root, old_pkg_path)
            if os.path.exists(empty_cynreub) and not os.listdir(empty_cynreub):
                os.rmdir(empty_cynreub)
                print(f"Removed empty directory: {empty_cynreub}")
                
            # Then, clean net (e.g., net)
            empty_net = os.path.join(full_src_root, old_pkg.split('.')[0])
            if os.path.exists(empty_net) and not os.listdir(empty_net):
                os.rmdir(empty_net)
                print(f"Removed empty directory: {empty_net}")

if __name__ == "__main__":
    project_root = os.path.abspath(os.path.dirname(__file__))
    rename_package(project_root, "net.cynreub", "xyz.drreub")
```

- [ ] **Step 2: Commit script creation**

We will commit this script separately if needed, or track it before running.

---

### Task 2: Execute Refactoring Script

**Files:**
- Modify: All source files and build configurations (auto-modified by `rename_namespace.py`)

- [ ] **Step 1: Execute Python refactoring script**

Run: `python3 rename_namespace.py`
Expected output: Lists directory creation, moves, and replacements in 25 files.

- [ ] **Step 2: Verify git status & file system state**

Run: `git status`
Expected: Shows package directories deleted (`net/`) and new directories added (`xyz/`), as well as modified build and layout files.

---

### Task 3: Build & Verification

**Files:**
- Verify: Entire project compiles and test suite passes

- [ ] **Step 1: Clean and compile the Android build**

Run: `./gradlew clean compileDebugSources --no-daemon -q`
Expected: Successful compile with exit code 0.

- [ ] **Step 2: Run Unit Tests**

Run: `./gradlew test --no-daemon`
Expected: PASS for all unit tests.

- [ ] **Step 3: Run Instrumented Test compile check**

Run: `./gradlew compileDebugAndroidTestSources --no-daemon -q`
Expected: Successful compile of instrumented tests with exit code 0.

- [ ] **Step 4: Remove script and complete refactor**

Run: `rm rename_namespace.py`
Expected: Temporary refactoring script is deleted.

---
