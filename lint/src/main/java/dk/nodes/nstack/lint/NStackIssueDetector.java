package dk.nodes.nstack.lint;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.UastLiteralUtils;

import java.util.Collections;
import java.util.List;

import dk.nodes.nstack.lint.issues.NStackTestIssue;

public class NStackIssueDetector extends Detector implements Detector.UastScanner {

    public static final List<Issue> ISSUES = Collections.singletonList(NStackTestIssue.ISSUE);


    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.singletonList(ULiteralExpression.class);
    }

    @Override
    public UElementHandler createUastHandler(JavaContext context) {
        // Note: Visiting UAST nodes is a pretty general purpose mechanism;
        // Lint has specialized support to do common things like "visit every class
        // that extends a given super class or implements a given interface", and
        // "visit every call site that calls a method by a given name" etc.
        // Take a careful look at UastScanner and the various existing lint check
        // implementations before doing things the "hard way".
        // Also be aware of context.getJavaEvaluator() which provides a lot of
        // utility functionality.
        return new UElementHandler() {
            @Override
            public void visitLiteralExpression(ULiteralExpression expression) {
                String string = UastLiteralUtils.getValueIfStringLiteral(expression);
                if (string == null) {
                    return;
                }

                if (string.contains("lint") && string.matches(".*\\blint\\b.*")) {
                    context.report(NStackTestIssue.ISSUE, expression, context.getLocation(expression),
                            "This code mentions `lint`: **Congratulations**");
                }
            }
        };
    }

}
