package br.com.felipeacerbi.buddies.models

import java.io.Serializable

data class PostInfo(val petId: String = "",
                    val message: String = "",
                    val photo: String = "",
                    val location: String = "") : Serializable