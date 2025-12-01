//
//  ios_offline_mapsApp.swift
//  ios-offline-maps
//
//  Created by Chris Whong on 9/17/25.
//

import SwiftUI

@main
struct ios_offline_mapsApp: App {
    init() {
        OfflineRegionManager.ensureStylePackDownloaded()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
