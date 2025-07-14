import { useState } from 'react';
import { useEffect } from 'react';
import inventory from '../dummy_data/inventory.json';

export default function Inventory(){
    const [data,setData]=useState(inventory.example);
    const [fourData,setFourData]=useState([]);

    useEffect(()=>{
        const sorted=[...data].sort((a,b)=>b.quantity-a.quantity);
        setFourData(sorted.slice(0, 4));
    },[data]);

    return(
        <div className="inventory-section">
            <h1 className="inventory">재고 물품 상위4개</h1>
            {fourData.map((a,index)=>(
                <div key={index} className="stocked-item">
                    <h2>{a.name}</h2>
                    <h4>재고:{a.quantity}개</h4>
                </div>
            ))}
        </div>
    );
}