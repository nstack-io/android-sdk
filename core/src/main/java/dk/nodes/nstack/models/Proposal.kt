package dk.nodes.nstack.models

data class Proposal(
        var id: Long,
        var applicationId: Long,
        var section: String,
        var key: String,
        var locale: String,
        var value: String
)