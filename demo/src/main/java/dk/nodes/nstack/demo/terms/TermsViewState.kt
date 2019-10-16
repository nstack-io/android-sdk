package dk.nodes.nstack.demo.terms

data class TermsViewState(
        val isLoading: Boolean,
        val errorMessage : String?,
        val termsName : String?,
        val termsContent: String?,
        val isAccepted: Boolean?
)