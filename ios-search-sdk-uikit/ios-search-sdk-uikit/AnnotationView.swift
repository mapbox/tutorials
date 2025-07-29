//
//  AnnotationView.swift
//  ios-search-sdk-uikit
//
//  Created by emilyryan on 7/29/25.
//


import Foundation
import UIKit
import MapboxSearch
import MapboxSearchUI
import MapboxMaps

// `AnnotationView` is a custom `UIView` subclass which is used only for annotation demonstration
class AnnotationView: UIView {
    
    var onSelect: ((Bool) -> Void)?
    var onClose: (() -> Void)?
    
    var selected: Bool = false {
        didSet {
            vStack.spacing = selected ? 20 : 4
            onSelect?(selected)
        }
    }
    
    var title: String? {
        get { centerLabel.text }
        set { centerLabel.text = newValue }
    }
    
    lazy var centerLabel: UILabel = {
        let label = UILabel(frame: .zero)
        label.font = UIFont.systemFont(ofSize: 10)
        label.numberOfLines = 0
        return label
    }()

    private let vStack: UIStackView
    
    override init(frame: CGRect) {
        vStack = UIStackView()
        super.init(frame: frame)
        backgroundColor = .white
        layer.shadowOpacity = 0.25
        layer.shadowRadius = 8
        layer.shadowOffset = CGSize(width: 0, height: 2)
        layer.cornerRadius = 8
        
        let hStack = UIStackView(arrangedSubviews: [centerLabel])
        hStack.spacing = 4
        
        vStack.addArrangedSubview(hStack)
        vStack.axis = .vertical
        vStack.translatesAutoresizingMaskIntoConstraints = false
        vStack.spacing = 4
        addSubview(vStack)
        NSLayoutConstraint.activate([
            vStack.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 4),
            vStack.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -4),
            vStack.topAnchor.constraint(equalTo: topAnchor, constant: 4),
            vStack.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -4),
        ])
        
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Action handlers
    
    @objc private func closePressed(sender: UIButton) {
        onClose?()
    }
    
    @objc private func selectPressed(sender: UIButton) {
        selected.toggle()
    }
    
}
