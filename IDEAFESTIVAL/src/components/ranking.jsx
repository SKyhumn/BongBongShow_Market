import { useState, useEffect } from 'react';
import ranking from '../dummy_data/gamedata.json';

export default function Ranking(){
    let [threeData, setThreeData]=useState([]);

    useEffect(()=>{
        const sorted=[...ranking.example].sort((a,b)=>{
            if(b.clickCount!==a.clickCount){
                return b.clickCount-a.clickCount;
            }
            else{
                return a.time-b.time;
            }
        });
        setThreeData(sorted.slice(0,3));
    },[ranking.example]);

    return(
        <div className="ranking-section">
            <h1>랭킹</h1>
            <div>
              {threeData.map((a,index)=>(
                <div className="Ranking" key={index}>
                  <h1 className="Num">{index+1}위</h1>
                  <div>
                    <h2>{a.name}</h2>
                    <span>클릭횟수:{a.clickCount}회</span>
                    <span>시간:00:{a.time}</span>
                  </div>
                </div>
              ))}
            </div>
        </div>
    );
}