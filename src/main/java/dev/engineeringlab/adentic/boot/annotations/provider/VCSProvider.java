package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a version control system provider implementation.
 *
 * <p>VCS providers handle low-level version control operations including commits, branches, tags,
 * and repository management.
 *
 * <p>The framework discovers all {@code @VCSProvider} annotated classes at runtime and registers
 * them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>Git:</b> Git version control
 *   <li><b>JGit:</b> Java implementation of Git
 *   <li><b>Subversion:</b> SVN version control
 *   <li><b>Mercurial:</b> Hg version control
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VCSProvider {

  /**
   * Unique name for this VCS provider.
   *
   * <p>Examples: "git", "jgit", "svn", "mercurial"
   *
   * @return provider name
   */
  String name();

  /**
   * Whether this provider supports branching.
   *
   * @return true if branching is supported
   */
  boolean supportsBranching() default true;

  /**
   * Whether this provider supports tagging.
   *
   * @return true if tagging is supported
   */
  boolean supportsTagging() default true;

  /**
   * Whether this provider supports merging.
   *
   * @return true if merging is supported
   */
  boolean supportsMerging() default true;

  /**
   * Whether this provider supports rebasing.
   *
   * @return true if rebasing is supported
   */
  boolean supportsRebasing() default false;

  /**
   * Whether this provider supports submodules.
   *
   * @return true if submodules are supported
   */
  boolean supportsSubmodules() default false;

  /**
   * Human-readable description of this VCS provider.
   *
   * @return provider description
   */
  String description() default "";

  /**
   * Selection priority when multiple providers are available.
   *
   * @return selection priority
   */
  int priority() default 0;

  /**
   * Whether this provider is enabled by default.
   *
   * @return true if enabled by default
   */
  boolean enabledByDefault() default true;
}
