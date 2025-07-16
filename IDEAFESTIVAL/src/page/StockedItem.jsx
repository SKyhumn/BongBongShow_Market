import inventory from '../dummy_data/inventory.json';
import './design/StockedItem.css';
import { useState, useEffect } from 'react';

export default function StockedItem(){
    const [sortedData,setSortedData]=useState([]);
    useEffect(()=>{
        const sorted=inventory.example.sort((a,b)=>b.quantity-a.quantity);
        setSortedData(sorted);
    },[inventory.example]);
    return(
        <div>
            <h1 className="StockedItemTitle">재고 물품 현황</h1>
            <div className="items">
                {sortedData.map((a,index)=>(
                    <div key={index} className="stocked">
                        <h2 className="name">{a.name}</h2>
                        <h3 className="price">{a.price}원<span className="quantity">{a.quantity}</span>개</h3>
                    </div>
                ))}
            </div>
        </div>
    );
}