package dk.nodes.nstack.lint.issues;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import dk.nodes.nstack.lint.NStackIssueDetector;

public final class NStackTestIssue {

     public static final Issue ISSUE = Issue.create(
             // ID: used in @SuppressLint warnings etc
             "ShortUniqueId",

             // Title -- shown in the IDE's preference dialog, as category headers in the
             // Analysis results window, etc
             "Lint Mentions",

             // Full explanation of the issue; you can use some markdown markup such as
             // `monospace`, *italic*, and **bold**.
             "This check highlights string literals in code which mentions " +
             "the word `lint`. Blah blah blah.\n" +
             "\n" +
             "Another paragraph here.\n",
    Category.CORRECTNESS,
            6,
    Severity.WARNING,
            new Implementation(
            NStackIssueDetector.class,
            Scope.JAVA_FILE_SCOPE));

}
