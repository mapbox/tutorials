//
//  BottomSheetUIView.swift
//  ios-search-sdk-uikit
//
//  Created by emilyryan on 7/29/25.
//


import UIKit

class BottomSheetUIView: UIView {
    
    public lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.text = "NAME"
        label.font = UIFont.systemFont(ofSize: 24)
        label.textColor = .white
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    public lazy var addressLabel: UILabel = {
        let label = UILabel()
        label.text = "ADDRESS"
        label.font = UIFont.systemFont(ofSize: 24)
        label.textColor = .white
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupViewHierarchy()
        setupViewAttributes()
        setupLayout()
        setHidden(hidden:true)
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.handleTap(_:)))
        addGestureRecognizer(tap)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupViewHierarchy(){
        self.addSubview(nameLabel)
        self.addSubview(addressLabel)
    }
    
    func setupViewAttributes(){
        self.backgroundColor = .black
        self.layer.cornerRadius = 20
    }
    
    func setupLayout(){
        NSLayoutConstraint.activate([
            nameLabel.centerXAnchor.constraint(equalTo: self.centerXAnchor),
            nameLabel.topAnchor.constraint(equalTo: self.topAnchor, constant: 50),
            addressLabel.centerXAnchor.constraint(equalTo: self.centerXAnchor),
            addressLabel.topAnchor.constraint(equalTo: self.topAnchor, constant: 100)
        ])
        
    }
    
    func setHidden(hidden: Bool)
    {
        self.isHidden = hidden
    }
    
    @objc func handleTap(_ sender: UITapGestureRecognizer? = nil) {
        if(!self.isHidden)
        {
            setHidden(hidden: true)
        }
    }
}
