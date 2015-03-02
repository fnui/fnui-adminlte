package fnui.adminlte.navigation

class Navigation {
    String name

    Navigation(String name) {
        this.name = name
    }

    Map<String,Navigation> categories = [:]
    List<NavigationItem> items = []
}
