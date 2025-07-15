import { Link } from "react-router-dom";
import Chart from '../components/chart.jsx';
import Inventory from '../components/inventory.jsx';
import Ranking from '../components/ranking.jsx';

export default function Home(){
    return(
        <div>
            <div className="sections">
                <Chart/>
                <Inventory/>
                <Ranking/>
            </div>
        </div>
    );
}