# Code Formatting with Spotless

This project uses [Spotless](https://github.com/diffplug/spotless) to enforce consistent code formatting across all files.

## What Spotless Formats

- **Java files**: Google Java Format style (2-space indentation)
- **XML/POM files**: Sorted and formatted consistently
- **YAML files**: Application configuration files
- **Markdown files**: Documentation files

## Quick Reference

### Check Formatting

Check if all files are properly formatted (without making changes):

```bash
mvn spotless:check
```

This command is automatically run during the `verify` phase of the Maven build.

### Apply Formatting

Automatically format all files according to the rules:

```bash
mvn spotless:apply
```

**Recommendation**: Run this command before committing your changes.

## Maven Lifecycle Integration

Spotless is integrated into the Maven build process:

- **`mvn verify`**: Runs `spotless:check` and fails the build if code is not properly formatted
- **`mvn test`**: Does not check formatting (tests only)
- **`mvn package`**: Runs `spotless:check` as part of the verify phase

## Common Workflows

### Before Committing Changes

```bash
# Format all files
mvn spotless:apply

# Verify formatting
mvn spotless:check

# Run tests to ensure nothing broke
mvn test

# Commit your changes
git add .
git commit -m "Your commit message"
```

### Fixing CI Build Failures

If the CI build fails with formatting violations:

```bash
# Apply formatting fixes
mvn spotless:apply

# Push the formatted code
git add .
git commit -m "Apply code formatting with Spotless"
git push
```

## Configuration

Spotless configuration is defined in `pom.xml` under the `spotless-maven-plugin` section:

### Java Formatting Rules

- **Style**: Google Java Format
- **Indentation**: 2 spaces
- **Import order**: java, javax, jakarta, org, com, then others
- **Trailing whitespace**: Removed
- **End of file**: Newline added

### File Patterns

```xml
<java>
  <!-- All .java files in src/main and src/test -->
</java>

<yaml>
  <includes>
    <include>src/**/*.yml</include>
    <include>src/**/*.yaml</include>
    <include>docker-compose.yml</include>
  </includes>
</yaml>

<markdown>
  <includes>
    <include>.github/**/*.md</include>
    <include>references/**/*.md</include>
    <include>README.md</include>
  </includes>
</markdown>
```

## IDE Integration

### IntelliJ IDEA

1. Install the **google-java-format** plugin from the marketplace
2. Go to **Settings > Other Settings > google-java-format Settings**
3. Check "Enable google-java-format"
4. Set code style to "Default Google Java Style"
5. Configure "Save Actions" plugin to format on save (optional)

### VS Code

1. Install the **Language Support for Java(TM)** extension
2. Install the **Spotless Gradle** extension (or use Maven commands)
3. Add to `settings.json`:

   ```json
   {
     "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml"
   }
   ```

### Eclipse

1. Download Google Java Format settings from:
   https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml
2. Import in **Preferences > Java > Code Style > Formatter**

## Troubleshooting

### Build fails with "format violations"

**Solution**: Run `mvn spotless:apply` to automatically fix formatting issues.

### Spotless changes my code in unexpected ways

**Reason**: Code doesn't follow Google Java Format style.

**Solution**:

- Review the changes with `git diff`
- If changes are acceptable, commit them
- If not, check if Spotless configuration needs adjustment

### I want to exclude certain files

Add patterns to `<excludes>` in the plugin configuration:

```xml
<java>
  <excludes>
    <exclude>src/main/java/com/example/GeneratedCode.java</exclude>
  </excludes>
</java>
```

## Benefits of Using Spotless

✅ **Consistency**: All team members write code in the same style  
✅ **No style debates**: Formatting is automated and enforced  
✅ **Clean diffs**: Git diffs show only logic changes, not formatting  
✅ **CI/CD integration**: Automatically catches formatting issues  
✅ **Multi-language**: Formats Java, YAML, Markdown, and more

## Related Commands

```bash
# Clean, format, and build
mvn clean spotless:apply install

# Format only Java files
mvn spotless:apply -DspotlessFiles=src/**/*.java

# Check formatting without building
mvn spotless:check -DskipTests

# Bypass Spotless check (not recommended)
mvn verify -Dspotless.check.skip=true
```

## Best Practices

1. **Run `mvn spotless:apply` before every commit**
2. **Configure your IDE** to use Google Java Format style
3. **Enable format-on-save** in your IDE for automatic formatting
4. **Don't bypass** `spotless:check` in CI builds
5. **Review formatting changes** before committing to understand what changed

---

For more information, visit the [Spotless documentation](https://github.com/diffplug/spotless/tree/main/plugin-maven).
