package com.farhanrr.makeda.share

import platform.UIKit.UIPasteboard

actual fun shareText(title: String, content: String) {
    UIPasteboard.generalPasteboard.string = content
}