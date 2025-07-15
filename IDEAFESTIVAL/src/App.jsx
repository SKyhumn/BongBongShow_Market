import './App.css';
import Home from './page/Home';
import PriceChanging from './page/PriceChanging.jsx';
import StockedItem from './page/StockedItem.jsx';
import Ranking from './page/Ranking.jsx';
import { BrowserRouter,Routes,Route,Link } from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <div className="Top">
          <Link to='/' className="LINK"><h1 className="TITLE">봉봉쇼마켓</h1></Link>
          <div className="Nav">
            <Link to='/price-changing' className="btn LINK">물가 변동 상세</Link>
            <Link to='/stocked-item' className="btn LINK">재고 물품</Link>
            <Link to='/ranking' className="btn LINK">랭킹</Link>
          </div>
        </div>
        <Routes>
          <Route path='/' element={<Home/>}></Route>
          <Route path='/price-changing' element={<PriceChanging/>}></Route>
          <Route path='/stocked-item' element={<StockedItem/>}></Route>
          <Route path='/ranking' element={<Ranking/>}></Route>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
