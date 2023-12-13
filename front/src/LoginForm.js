function LoginForm({onLogged}) {

    function onClick() {
        alert("Logged");
        onLogged();
    }

    return (
        <button onClick={onClick}>Log in</button>
    );
}

export default LoginForm;