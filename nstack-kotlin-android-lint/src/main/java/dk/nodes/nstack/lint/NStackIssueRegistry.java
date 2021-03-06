package dk.nodes.nstack.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;
import java.util.List;

public class NStackIssueRegistry extends IssueRegistry {
    @Override
    public List<Issue> getIssues() {
        return NStackIssuesDetector.Companion.getISSUES();
    }

    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }
}
