git.useGitDescribe := true

val releaseTagRegex     = raw"""v(d+.d+.d+)""".r
val developmentTagRegex = raw"""v(d+.d+.d+-d+-g[0-9a-f]{7})""".r

val sanitizedGitCurrentBranch = Def.setting {
  git.gitCurrentBranch.value.split('/').mkString("-")
}

git.gitTagToVersionNumber := {
  case releaseTagRegex(tag)     => Some(tag)
  case developmentTagRegex(tag) => Some(s"$tag-${sanitizedGitCurrentBranch.value}")
  case tag                      => Some(s"$tag-${sanitizedGitCurrentBranch.value}")
}
