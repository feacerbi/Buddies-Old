package br.com.felipeacerbi.buddies.models

data class PostInfo(val petId: String = "",
                    val message: String = "",
                    val photo: String = "",
                    val location: String = "")