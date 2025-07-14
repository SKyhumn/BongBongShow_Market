import './App.css';
import Home from './page/Home';
import { BrowserRouter,Routes,Route } from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path='/' element={<Home/>}></Route>
          <Route path='/Home' element={<Home/>}></Route>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
