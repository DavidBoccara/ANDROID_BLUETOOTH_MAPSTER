package fr.esme.esme_map.model

class Run (
    val owner: User,
    val name: String,
    var score: Int,
    var positions: List<Position>,
    var category: Category
)