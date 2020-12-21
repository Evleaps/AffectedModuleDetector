package com.dropbox.affectedmoduledetector.commitshaproviders

import com.dropbox.affectedmoduledetector.GitClient
import com.dropbox.affectedmoduledetector.Sha

class ForkCommit: CommitShaProvider {
    override fun getCommitSha(commandRunner: GitClient.CommandRunner): Sha? {
        val currentBranch = commandRunner.executeAndParse(CURRENT_BRANCH_CMD).firstOrNull()

        requireNotNull(currentBranch) {
            "Current branch not found"
        }

        val parentBranch = commandRunner.executeAndParse(SHOW_ALL_BRANCHES_CMD)
            .first { !it.contains(currentBranch) && it.contains("*") }
            .substringAfter("[")
            .substringBefore("]")
            .substringBefore("~")
            .substringBefore("^")

        require(parentBranch.isNotEmpty()) {
            "Parent branch not found"
        }

        return commandRunner.executeAndParse("git merge-base $currentBranch $parentBranch")
            .firstOrNull()
            ?.split(" ")
            ?.firstOrNull()
    }

    companion object {
        const val CURRENT_BRANCH_CMD = "git rev-parse --abbrev-ref HEAD"
        const val SHOW_ALL_BRANCHES_CMD = "git show-branch -a"
    }
}