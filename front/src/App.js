import './App.css';
import {useState} from "react";
import LoginForm from "./LoginForm";
import {useCookies} from "react-cookie";

function App() {
    const [isLogged, setLogged] = useState(false);
    const [cookies, setCookie] = useCookies(["token", "isLogged"]);

    if (!isLogged && cookies.isLogged === true)
        setLogged(true);

    function onLogged() {// TODO sign in actually
        setCookie("token", "sometoken", { httpOnly: true, sameSite: true, maxAge: 60 });
        setCookie("isLogged", true, { sameSite: true, maxAge: 60 });
        setLogged(true);
    }

    return isLogged ? (
        <p>You are logged</p>
    ) : (
        <div className="App">
            <p>App Div</p>
            <LoginForm onLogged={onLogged}/>
        </div>
    );
}

export default App;
