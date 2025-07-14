import { useState } from "react";
import { useEffect } from "react";
import '../App.css';
import priceChange from '../dummy_data/priceChange.json';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from 'recharts';

export default function Chart(){
    let [data,setData]=useState([]);
    let [min,setMin]=useState(null);

    //물품들 중 현재 가격이 가장 낮은 가격
    useEffect(()=>{
        let currentMin=Infinity;

        for(const item of priceChange.example){
            const lastChange = item["price-change"].at(-1).y;
            if (lastChange < currentMin) {
                currentMin=lastChange;
            }
        }
        setMin(currentMin);
    },[priceChange]);
    
    //가장 낮은 가격의 물품 데이터 추출
    useEffect(()=>{
        if(min==null) return;
        
        const filtered=priceChange.example.filter(a=>a["price-change"].at(-1).y===min);
        setData(filtered)
    },[min]);

    return(
        <div className="chart-section">
            {data.length===0?
                (<p>데이터 로딩중</p>):
                (<div>
                    <h1>현재 최저가 물품</h1>
                    <h3><span className="item">{data[0].name}</span>의 시세</h3>
                    <div className="priceChart">
                        {/*추출한 data를 바탕으로 차트 만들기*/}
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={data[0]["price-change"]}>
                                <CartesianGrid stroke="#ccc" strokeDasharray="3 3" />
                                <XAxis dataKey="x" />
                                <YAxis />
                                <Tooltip />
                                <Line type="linear" dataKey="y" stroke="#8884d8" strokeWidth={2} />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                    <h3><span className="item">{data[0].name}</span>의 현재 가격:{min}원</h3>
                    <h3>가격 변동률:{(((min-data[0]["price-change"].at(-2).y)/data[0]["price-change"].at(-2).y)*100).toFixed(2)}%</h3>
                </div>)
            }
        </div>
    );
}