package fnui.adminlte.navigation

class NavigationTagLib {
    static namespace = "adminlte"
    static defaultEncodeAs = [taglib:'none']

    NavigationService navigationService

    def mainNavigation = { attrs ->
        def navigation = navigationService?.navigation

        if (!navigation) {
            return
        }

        navigation.categories.each { name, category ->
            renderNavigationCategory(category)
        }

        navigation.items.each { item ->
            renderNavigationItem(item)
        }
    }

    private renderNavigationCategory(Navigation category) {
        out << """\
<li class="treeview">
    <a href="#">
        <span>${category.name}</span>
        <i class="fa fa-angle-left pull-right"></i>
    </a>
    <ul class="treeview-menu">
"""
        category.items.each { item ->
            renderCategoryItem(item)
        }
        out << '''\
    </ul>
</li>
'''
    }

    private renderNavigationItem(NavigationItem item) {
        out << '<li>'
        out << g.link(item.params) {
            out << "<span>${item.name}</span>"
        }
        out << '</li>'
    }

    private renderCategoryItem(NavigationItem item) {
        out << '<li>'
        out << g.link(item.params) {
            out << '<i class="fa fa-angle-double-right"></i>'
            out << item.name
        }
        out << '</li>'
    }
}
