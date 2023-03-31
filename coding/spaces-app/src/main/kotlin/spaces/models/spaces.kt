package spaces.models

data class SpacePreview(
    val spaceId: String,
    val title: String,
    val address: String
)

data class SpaceModel(
    val spaces: List<SpacePreview>
)

object SpacesModel {
    val empty: SpaceModel = SpaceModel(
        listOf(
            SpacePreview(
                spaceId = "space-001",
                title = "Coworking Castellana",
                address = "plaza castilla2"
            ),
            SpacePreview(
                spaceId = "space-002",
                title = "Coworking Cuzco",
                address = "plaza cuzco"
            )
        )
    )
}
