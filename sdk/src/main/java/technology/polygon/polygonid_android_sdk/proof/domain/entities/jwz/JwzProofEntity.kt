package technology.polygon.polygonid_android_sdk.proof.domain.entities.jwz

import kotlinx.serialization.Serializable

@Serializable
open class JwzBaseProof {
    var piA: List<String>
    var piB: List<List<String>>
    var piC: List<String>
    var protocol: String
    var curve: String

    constructor(
        piA: List<String>,
        piB: List<List<String>>,
        piC: List<String>,
        protocol: String,
        curve: String
    ) {
        this.piA = piA
        this.piB = piB
        this.piC = piC
        this.protocol = protocol
        this.curve = curve
    }
}


@Serializable
class JwzProofEntity : JwzBaseProof {
    var pubSignals: List<String>

    constructor(
        piA: List<String>,
        piB: List<List<String>>,
        piC: List<String>,
        protocol: String,
        curve: String,
        pubSignals: List<String>
    ) : super(piA, piB, piC, protocol, curve) {
        this.pubSignals = pubSignals
    }
}
