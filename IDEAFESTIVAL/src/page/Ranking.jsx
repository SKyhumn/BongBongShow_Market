import ranking from '../dummy_data/gamedata.json';
import './design/Ranking.css';
import { useState,useEffect } from 'react';


export default function Ranking(){
    const[sortedData,setSortedData]=useState([]);
    useEffect(()=>{
        const sorted=ranking.example.sort((a,b)=>{
            if(a.clickCount!==b.clickCount){
                return b.clickCount-a.clickCount;
            }
            else{
                return a.time-b.time;
            }
        })
        setSortedData(sorted);
    },[ranking.example]);
    return(
        <div>
            <h1 className="RankingTitle">랭킹</h1>
            {sortedData.map((a,index)=>(
                <div key={index} className="box">
                    <span className="number">{index+1}</span>
                    <span className="itemName">{a.name}</span>
                    <span className="countAndTime">클릭횟수:{a.clickCount}</span>
                    <span className="countAndTime">시간:00:{a.time}</span>
                </div>
            ))}
        </div>
    );
}