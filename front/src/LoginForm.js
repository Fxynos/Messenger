function LoginForm({onLogged}) {
    let address = require("./Configuration").address

    function onClick() {
        alert(address);
        onLogged();
    }

    return (
        <button onClick={onClick}>Log in</button>
    );
}

export default LoginForm;