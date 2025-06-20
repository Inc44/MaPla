sealed interface Routes {
    val route: String

    object SignIn : Routes {
        override val route = "sign_in"
    }

    object Picker : Routes {
        override val route = "picker"
    }

    object Editor : Routes {
        override val route = "editor"
    }

    object Player : Routes {
        override val route = "player"
    }
}
