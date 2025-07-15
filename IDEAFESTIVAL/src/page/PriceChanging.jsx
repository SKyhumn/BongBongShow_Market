import priceChange from '../dummy_data/priceChange.json';
import './design/PriceChanging.css';
import { useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from 'recharts';

export default function PriceChanging(){
    const [select, setSelected]=useState(null);
    const [data, setData]=useState([]);
    const clicked=(index)=>{
        setSelected(index);
        const chartData=priceChange.example[index]["price-change"];
        setData(chartData);
    };
    return(
        <div className="section">
            <div className="selection">
                {priceChange.example.map((a,index)=>(
                    <p key={index} onClick={()=>clicked(index)} style={{color:select===index?'rgb(57, 80, 255)':'black'}}>{a.name}</p>
                ))}
            </div>
            {select!==null&&(            
                <div className="chart">
                    <h1><span className="item">{priceChange.example[select].name}</span>의 가격변동</h1>
                    <h3 className="changeRate">가격 변동률:{(((data.at(-1).y-data.at(-2).y)/data.at(-2).y)*100).toFixed(2)}%</h3>
                    <ResponsiveContainer width="100%" height={400} className="priceChart">
                        <LineChart data={data}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="x" />
                            <YAxis />
                            <Tooltip />
                            <Line type="linear" dataKey="y" stroke="#3a6cf4" strokeWidth={2} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            )}
        </div>
    );
}