import SwiftUI
import Shared

@main
struct iOSApp: App {
    private let ads = GoogleAdsBridge()

    init() {
        MainViewControllerKt.startApp(adsBridge: ads)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
                .onOpenURL { url in _ = MainViewControllerKt.handleDeepLink(url: url.absoluteString) }
        }
    }
}
