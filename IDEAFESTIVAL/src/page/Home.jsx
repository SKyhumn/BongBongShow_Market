import { Link } from "react-router-dom";
import Chart from '../components/chart.jsx';
import Inventory from '../components/inventory.jsx';
import Ranking from '../components/ranking.jsx';

export default function Home(){
    return(
        <div>
            <div className="Top">
                <Link to='/Home' className="LINK"><h1 className="TITLE">봉봉쇼마켓</h1></Link>
                <div className="Nav">
                    <Link className="LINK"><p className="btn">물가 변동 상세</p></Link>
                    <Link className="LINK"><p className="btn">재고 물품</p></Link>
                    <Link className="LINK"><p className="btn">랭킹</p></Link>
                </div>
            </div>
            <div className="sections">   
                <Chart/>
                <Inventory/>
                <Ranking/>
            </div>
        </div>
    );
}