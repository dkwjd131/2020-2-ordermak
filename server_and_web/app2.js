const mariadb = require('mariadb/callback');
var express = require('express'); //json 을 파싱하기 위한 프레임워크
var bodyParser = require('body-parser'); // 미들웨어
const socketio = require('socket.io');// 소켓통신으로 주문내역 전송
const http = require('http');
//const ioclient = require('socket.io-client'); 
const fs = require('fs');//파일을 전달할 수 있는 모듈
serveStatic = require('serve-static'); //웹페이지에서 사용
var path = require('path');
var session = require('express-session');
var app = express();
var server = http.createServer(app);
var io = socketio(server);//socket.io랑 서버랑 연결



// ejs를 사용하여 특정 웹페이지로 변수와 값을 전달
app.set('view engine', 'ejs');
app.engine('html', require('ejs').renderFile);


// body-parser : post로 요쳥했을 때의 요청 파라미터 확인 방법을 제공
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));


// 웹페이지가 해당 폴더에 접근할 수 있도록 허용
app.use('/css',express.static('/home/ubuntu/node-project/dg/static/css')); 
app.use('/js',express.static('/home/ubuntu/node-project/dg/static/js'));
app.use(express.static('/home/ubuntu/node-project/dg/public'));


//serve-static을 사용하여 HTML 폴더를 /public,/static의 path로 접근할 수 있도록 함
app.use('/public', serveStatic(path.join(__dirname,'static')));
app.use('/static',express.static('/home/ubuntu/node-project/dg/static'));


//웹페이지 로그인 유지 관련 session인데 뭔지 잘 모르겠음
app.use(session({
    secret: '@#@$MYSIGN#@$#$',
    resave: false,
    saveUninitialized: true}));
   

//라우터 사용
var router = express.Router();


// 안드로이드 스튜디오 포트 연결
app.listen(3000,'ec2-3-34-129-172.ap-northeast-2.compute.amazonaws.com',function(){ 
	console.log('3000 서버 실행중');
});


//데이터베이스 연결
var connection = mariadb.createConnection({
	host:"dgmariadb.cg01cezwwj3b.ap-northeast-2.rds.amazonaws.com",
	user:"dkwjd",
	database:"capstonedb",
	password:"dkwjddl131",
    port: 3306,
    multipleStatements:true
});





//소켓 통신
io.sockets.on('connection',function(socket){//서버와 클라이언트가 연결되었을때 실행
	const { url } = socket.request;
	console.log('연결됨 url: '+url);
	//socket.on("text",function(text){ // 클라이언트에서 서버로 오는 요청을 처리하는 이벤트리스더
      //  console.log('메세지: '+text.mm);});
        //io.emit()
});


//이후는 모바일 웹과 웹페이지에서 사용하는 api(post) 정의

//웹페이지에: 처음 웹페이지에 접근할 수 있는 주소 등록 함수
app.get('/order',function(req,res){ 
    //req는 클라이언트에서 전달된 데이터와 정보들이 담겨있다
    // res는 클라이언트에게 응답을 위한 정보가 들어있다

    fs.readFile('/home/ubuntu/node-project/dg/static/index.ejs',function(err,data){
        if(err){
            res.send('error');
            console.log('get(/order)에서 에러 발생');
        }else{
            res.writeHead(200, {'Content-Type':'text/html'});
            res.write(data);
            res.end();
        }
    });
});





//웹페이지: 회원가입 버튼을 눌러서 회원가입 페이지로 넘어가는 함수
router.route('/goSign').post(
    function (req, res) {
        console.log('goSign 호출됨');

        res.render('/home/ubuntu/node-project/dg/static/Sign', {tmp:"signpage"}); 
    }
);





//웹페이지: 단순 main 페이지로 넘어가는 함수
router.route('/goMain').post(
    function (req, res) {
        console.log('goMain 호출됨');

        var owner_num = req.body.owner_num;

        res.render('/home/ubuntu/node-project/dg/static/main', {owner_num:owner_num}); 
    }
);





//웹페이지: 회원가입
router.route('/process/addUser').post(
    function (req, res)
    {
        console.log('process/addUser 호출됨');

        var idowner = req.body.idowner;
        var password = req.body.password;
	    var name = req.body.name;
	    var phone_num = req.body.phone_num;
        var email = req.body.email;

        addUser(idowner,password,name,phone_num,email,
            function (err, result) {
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생 - 이미 존재하는  아이디일수 있음</h1>');
                    res.write('<br><a href="/login.html"> re login </a>');
                    res.end();
                    return;
                }
 
                if (result){
                    io.emit("join",{result:"menu_order"});//클라이언트로 전송
                    res.render('/home/ubuntu/node-project/dg/static/index', {tmp:"회원가입 완료"});
                }else
                {
                    console.log('데이터베이스에 추가 에러');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1> Failed : add user</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 로그인
router.route('/process/login').post(
    function (req, res) {
        console.log('process/login 호출됨');

        var idowner = req.body.idowner;
        var password = req.body.password;

        
        authUser( idowner, password,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }

                if (rows) {
                    console.dir(rows);
                    res.render('/home/ubuntu/node-project/dg/static/main', { owner_num:rows[0].owner_num });
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 매장 이름 선택 시 호출되는 함수
//store_num을 넘겨서 매장 정보를 관리할 수 있도록 한다
router.route('/sendstorename').post(
    function (req, res) {
        console.log('sendstorename 호출됨');

        var store_num = req.body.store_num;
        var owner_num = req.body.owner_num;

        storeInfo( store_num,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    console.dir(rows);
                    res.render('/home/ubuntu/node-project/dg/static/manage', {owner_num:owner_num,
                        store_num:store_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 매장관리 버튼을 누르면 매장 목록을 띄우는 함수
router.route('/sendstorelist').post(
    function (req, res) {
        console.log('sendstorelist 호출됨');

        var owner_num = req.body.owner_num;

        storeList( owner_num,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    console.dir(rows);
                    res.render('/home/ubuntu/node-project/dg/static/select', {owner_num:owner_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 매장 정보 수정 버튼을 눌러서 새로운 정보가 DB에 반영되도록 하는 함수
router.route('/changestoreinfo').post(
    function (req, res) {
        console.log('changestoreinfo 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;
        var store_name = req.body.store_name;
        var beacon_ID = req.body.beacon_ID;
        var stevt = req.body.stevt;

        changestoreInfo( store_num,owner_num,store_name,beacon_ID,stevt,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    console.dir(rows);
                    io.emit("storeInfoChange",{result:"menu_order"});//클라이언트로 전송
                    res.render('/home/ubuntu/node-project/dg/static/storeInfo', {store_num:store_num, 
                        owner_num:owner_num,
                        store_name:store_name,
                        beacon_ID:beacon_ID,
                        stevt:stevt});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        ); 
    }
);





//웹페이지: 매장 삭제 버튼을 눌렀을 때 호출되는 함수
router.route('/storedelete').post(
    function (req, res) {
        console.log('storedelete 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;

        storedelete( store_num,owner_num,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    console.dir(rows);
                    io.emit("storedelete",{result:"storedelete"});//클라이언트로 전송
                    res.render('/home/ubuntu/node-project/dg/static/select', {owner_num:owner_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        ); 
    }
);





//웹페이지: 메인 -> 매장 관리 페이지 -> 매장 정보 수정 버튼을 누르면 호출되는 함수 
router.route('/manage').post(
    function (req, res) {
        console.log('manage 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;
        var store_name = req.body.store_name;
        var beacon_ID = req.body.beacon_ID;
        var stevt = req.body.stevt;

        res.render('/home/ubuntu/node-project/dg/static/storeInfo', {store_num:store_num, 
            owner_num:owner_num,
            store_name:store_name,
            beacon_ID:beacon_ID,
            stevt:stevt
        });
    }
);





//웹페이지: 매장 등록 위해 owner_num을 넘기는 함수
router.route('/registerstore1').post(
    function (req, res) {
        console.log('registerstore1 호출됨');

        var owner_num = req.body.owner_num;

        res.render('/home/ubuntu/node-project/dg/static/registeration', {owner_num:owner_num,}); 
    }
);





//웹페이지: 입력한 매장 정보를 DB에 저장하는 함수
router.route('/registerstore2').post(
    function (req, res) {
        console.log('registerstore2 호출됨');

        var owner_num = req.body.owner_num;
        var store_name = req.body.store_name;
        var beacon_ID = req.body.beacon_ID;
        var stevt = req.body.stevt;

        registerstore2( owner_num,store_name,beacon_ID,stevt,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    console.dir(rows);
                    res.render('/home/ubuntu/node-project/dg/static/select', {owner_num:owner_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 메인 -> 매장 관리 페이지 -> 메뉴 관리 버튼을 누르면 호출되는 함수 
router.route('/menumanage').post(
    function (req, res) {
        console.log('menumanage 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;
        var store_name = req.body.store_name;
        var beacon_ID = req.body.beacon_ID;
        var stevt = req.body.stevt;

        menumanage( store_num,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    res.render('/home/ubuntu/node-project/dg/static/menu', {owner_num:owner_num,
                        store_num:store_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 메뉴 삭제 버튼 클릭시 후출되는 함수
router.route('/menudelete').post(
    function (req, res) {
        console.log('menudelete 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;
        var menu_num = req.body.menu_num;
        var menu_name = req.body.menu_name;
        var category = req.body.category;
        var price = req.body.price;
        var description = req.body.description;
        var data = req.body.data;
        var result = "삭제"

        menudelete( menu_num,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {

                    sql2 = 'SELECT * FROM menus WHERE store_num = ?';
                    connection.query(sql2, store_num, function(err,result){
                        
                        if(result.length >=0){
                            io.emit("menudelete",{text:"menudelete"});
                            res.render('/home/ubuntu/node-project/dg/static/menu', {owner_num:owner_num,
                                store_num:store_num,
                                data:result});
                        }
                    })
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 메뉴 수정 버튼 클릭시 호출되는 함수
router.route('/menuchange').post(
    function (req, res) {
        console.log('menuchange 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;
        var menu_num = req.body.menu_num;
        var menu_name = req.body.menu_name;
        var category = req.body.category;
        var price = req.body.price;
        var description = req.body.description;

        menuchange( menu_num,menu_name,price,category,description,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    
                    sql2 = 'SELECT * FROM menus WHERE store_num = ?';
                    connection.query(sql2, store_num, function(err,result){
                        
                        if(result.length >=0){
                            io.emit("menuchange",{text:"menuchange"});
                            res.render('/home/ubuntu/node-project/dg/static/menu', {owner_num:owner_num,
                                store_num:store_num,
                                data:result});
                        }
                    })

                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        );
    }
);





//웹페이지: 메뉴 추가하기 버튼 클릭시 호출되는 함수
router.route('/goaddmenu').post(
    function (req, res) {
        console.log('goaddmenu 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;

        res.render('/home/ubuntu/node-project/dg/static/addmenu', {owner_num:owner_num, store_num:store_num});
    }
);





//웹페이지: 새로운 메뉴 정보를 등록하고 버튼 클릭시 호출되는 함수
router.route('/addmenu').post(
    function (req, res) {
        console.log('addmenu 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;
        var menu_name = req.body.menu_name;
        var price = req.body.price;
        var category = req.body.category;
        var description = req.body.description;
        var result = '메뉴 추가';

        addmenu( store_num,menu_name,price,category,description,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    io.emit("addmenu",{text:"addmenu"});
                    res.render('/home/ubuntu/node-project/dg/static/menu', {owner_num:owner_num,
                        store_num:store_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        ); 
    }
);





//웹페이지: 메인 -> 매장 관리 페이지 -> 주문 확인 버튼을 누르면 호출되는 함수
router.route('/goOrderConfirm').post(
    function (req, res) {
        console.log('goOrderConfirm 호출됨');

        var owner_num = req.body.owner_num;
        var store_num = req.body.store_num;

        goOrderConfirm( store_num,
            function (err, rows){
                if (err) {
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }
 
                if (rows) {
                    res.render('/home/ubuntu/node-project/dg/static/OrderConfirmation', {owner_num:owner_num,
                        store_num:store_num,
                        data:rows});
                }else {
                    console.log('empty Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>user data not exist</h1>');
                    res.write('<a href="/login.html"> re login</a>');
                    res.end();
                }
            }
        ); 
    }
);





//웹페이지: 주문 상태 변경 후 저장 버튼 누르면 호출되는 함수
router.route('/statechange').post(
    function (req, res) {
        console.log('statechange 호출됨');

        var order_num = req.body.order_num;
        var state = req.body.state;
        var store_num = req.body.store_num;
        var owner_num = req.body.owner_num;

        var sql = 'UPDATE orders SET state = ? WHERE order_num = ?';
        var params = [state,order_num];

        connection.connect(function (err){
            if (err) throw err;
           
            connection.query(sql,params,function(err,result){
                
                if(err){
                    console.log('Error!!!');
                    res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                    res.write('<h1>에러발생</h1>');
                    res.end();
                    return;
                }else{

                    goOrderConfirm( store_num,
                        function (err, rows){
                            if (err) {
                                console.log('Error!!!');
                                res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                                res.write('<h1>에러발생</h1>');
                                res.end();
                                return;
                            }
             
                            if (rows) {
                                io.emit("statechange",{text:"statechange"});
                                res.render('/home/ubuntu/node-project/dg/static/OrderConfirmation', {owner_num:owner_num,
                                    store_num:store_num,
                                    data:rows});
                            }else {
                                console.log('empty Error!!!');
                                res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
                                res.write('<h1>user data not exist</h1>');
                                res.write('<a href="/login.html"> re login</a>');
                                res.end();
                            }
                        }
                    );
                }
            });
        });    
    }
);





//라우터 미들웨어 등록하는 구간에서는 라우터를 모두  등록한 이후에 다른 것을 세팅한다
//그렇지 않으면 순서상 라우터 이외에 다른것이 먼저 실행될 수 있다
app.use('/', router);       //라우트 미들웨어를 등록한다





//웹페이지: 회원가입
var addUser = function(idowner,password,name,phone_num,email,callback){

    console.log('addUser 호출');
    var sql = 'INSERT INTO Owners (idowner,password,name,phone_num,email) VALUES (?,?,?,?,?)';
    var params = [idowner,password,name,phone_num,email];

    connection.connect(function (err){
            if (err) throw err;
           
            connection.query(sql,params,function(err,result){
                
                if(err){
                    console.log(err);
                    callback(null, result);
                }else{
                    callback(null, result);
                }
            });
        }
    );
}





//웹페이지: 로그인
var authUser = function (idowner, password, callback) {

    var sql = 'SELECT * FROM Owners WHERE idowner = ?';
    
    connection.connect(function(err){ 
        if(err) throw err;
        
        connection.query(sql, idowner, function(err,result){
            
            if(err){
                console.log(err);
            }else{
                if(result.length ===0){
                    console.log('존재하지 않는 계정입니다.');
                    callback(null, null);
                }else if (password !== result[0].password){
                    console.log('비밀번호가 틀렸습다.');
                    callback(null, null);
                }else{
                console.log('로그인 성공!' + result[0].idowner + '님 환영 합니다!');
                callback(null, result);
                }
            }
        });
    }); 
};





//웹페이지: 매장 목록 출력
var storeList = function (owner_num, callback) {
    
    var sql = 'SELECT * FROM stores WHERE owner_num = ?';
    
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, owner_num, function(err,result){
            
            if(err){
                console.log(err);
            }else{
                if(result.length ===0){
                    console.log('등록된 매장이 없습니다.');
                    callback(null, null);
                }else {
                console.log('등록한 매장 목록 검색 완료');
                callback(null, result);
			    }
		    }

        });
    });
};





//웹페이지: 매장 정보 출력
var storeInfo = function (store_num,callback) {
    
    var sql = 'SELECT * FROM stores WHERE store_num = ?';

    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, store_num, function(err,result){

            if(err){
                console.log(err);
            }else{
                if(result.length ===0){
                    console.log('등록된 매장이 없습니다.');
                    callback(null, null);
                }else {
                console.log('등록한 매장 정보 검색 완료');
                callback(null, result);
			    }
		    }
        });
    });
};





//웹페이지: 매장 정보 수정
var changestoreInfo = function (store_num,owner_num,store_name,beacon_ID,stevt, callback) {
    
    var params = [store_name,beacon_ID,stevt,store_num];
    var sql = 'UPDATE stores SET store_name =?, beacon_ID=?, stevt=? WHERE store_num = ?';
    var sql2 = 'SELECT * FROM stores WHERE store_num = ?';

    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, params, function(err,result){
            
            if(err){
                console.log(err);
            }else{
                console.log('매장 정보 수정 완료');

                connection.query(sql2, store_num, function(err,result){
            
                    if(err){
                        console.log(err);
                    }else{
                        console.log('매장 정보 검색 완료');
                        callback(null, result);
                    }
                });
		    }
        });
    });
};





//웹페이지: 매장 삭제
var storedelete = function (store_num,owner_num, callback) {
    
    var sql = 'DELETE FROM stores WHERE store_num = ?';
    var sql2 = 'SELECT * FROM stores WHERE owner_num = ?';

    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, store_num, function(err,result){
            
            if(err){
                console.log(err);
            }else{
                console.log('매장 삭제 완료');

                connection.query(sql2, owner_num, function(err,result){
            
                    if(err){
                        console.log(err);
                    }else{
                        console.log('매장 목록 검색 완료');
                        callback(null, result);
                    }
                });
		    }
        });
    });
};





//웹페이지: 매장 등록
var registerstore2 = function (owner_num,store_name,beacon_ID,stevt, callback) {
    
    var params = [owner_num,store_name,beacon_ID,stevt];
    var sql = 'INSERT into stores (owner_num, store_name, beacon_ID, stevt) values(?,?,?,?)';
    
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, params, function(err,result){
            
            if(err){
                console.log(err);
            }else{
                console.log('매장 등록 완료');
                var sql2 = 'SELECT * FROM stores WHERE owner_num = ?';

                connection.query(sql2, owner_num, function(err,result){
            
                    if(err){
                        console.log(err);
                    }else{
                        console.log('매장 목록 검색 완료');
                        callback(null, result);
                    }
                });
		    }
        });
    });
};





//웹페이지: 메뉴 관리 함수
var menumanage = function (store_num, callback) {
    
    var sql = 'SELECT * FROM menus WHERE store_num = ?';
    
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, store_num, function(err,result){
            
            if(err){
                console.log(err);
            }else{
                console.log('메뉴 검색 완료');
                callback(null, result);
		    }
        });
    });
};





//웹페이지: 메뉴 삭제 함수
var menudelete = function (menu_num,callback) {
    
    var sql = "DELETE FROM menus WHERE menu_num =?";
    
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, menu_num,function(err,result){

            if(err){
                console.log(err);
            }else{
                console.log('메뉴 삭제 완료');
                callback(null, result);
		    }
        });
    });
};





//웹페이지: 아마 지울 것 가
var backtomenu = function (store_num,callback) {
    
    var sql = 'SELECT * FROM menus WHERE store_num = ?';
  
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, store_num,function(err,result){
            var resultCode = 404;
            var message = '에러가 발생했따';
            
            if(err){
                console.log(err);
            }else{
                
				resultCode = 200;
                console.log('다시 메뉴 목록으로 돌아가기');
                callback(null, result);
		}

    });
});
};





//웹페이지: 메뉴 수정 함수
var menuchange = function (menu_num,menu_name,price,category,description,callback) {
    
    params=[menu_name,price,category,description,menu_num];
    var sql = 'UPDATE menus SET menu_name =?, price=?, category=?, description=? WHERE menu_num = ?';
    var sql2 = 'UPDATE menus SET menu_name =?, price=?, category=?, description=? WHERE menu_num = ?';
    
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, params,function(err,result){
            if(err){
                console.log(err);
            }else{
                console.log('메뉴 수정 완료');
                callback(null, result);
		    }
        });
    });
};





//웹페이지: 메뉴 등록 함수
var addmenu = function (store_num,menu_name,price,category,description, callback) {
    
    var params = [store_num,menu_name,price,category,description];
    var sql = 'INSERT into menus (store_num, menu_name, price, category,description) values(?,?,?,?,?)';
    var sql2 = 'SELECT * FROM menus WHERE store_num = ?';

    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, params, function(err,result){
            if(err){
                console.log(err);
            }else{
                console.log('메뉴 등록 완료');

                connection.connect(function(err){ // 데이터베이스랑 연결
                    if(err) throw err;
                    
                    connection.query(sql2, store_num, function(err,result){
                        if(err){
                            console.log(err);
                        }else{
                            console.log('메뉴 검색 완료');
                            callback(null, result);
                        }
                    });
                });
		    }
        });
    });
};





//웹페이지: 주문 내역 호출 함수
var goOrderConfirm = function (store_num, callback) {
    
    var sql = 'SELECT * FROM orders WHERE store_num = ?';
    
    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql, store_num, function(err,result){
            if(err){
                console.log(err);
            }else{
                console.log('주문 내역 검색 완료');
                callback(null, result);
		    }
        });
    });
};










//웹페이지 80번 포트 열기
server.listen(80,function(){ 
	console.log('80 conn');
});




//모바일앱: 회원가입
app.post('/user/join',function(req,res){
	console.log(req.body);
	var userName = req.body.userName;
	var userPhone = req.body.userPhone;
	var userID = req.body.userID;
	var userPwd = req.body.userPwd;

	var sql = 'INSERT INTO Users (UserName, UserPhone,UserID,UserPwd) VALUES (?,?,?,?)';
	var params = [userName, userPhone, userID, userPwd];

	connection.connect(function(err){ // 데이터베이스랑 연결
		if(err) throw err;
        
		connection.query(sql,params,function(err,result){
            var resultCode = 404;
            var message = '회원가입 에러 발생';
            
            if(err){
                console.log(err);
            }else{
                resultCode = 200;
                message = '회원가입 성공';
            }
            
            res.json({
                'code':resultCode,
                'message': message
            });
	    });
    });
});





//모바일앱: 로그인
app.post('/user/login',function(req,res){

	var userID =  req.body.userID ;
    var userPwd = req.body.userPwd;
    var sql = 'SELECT * FROM Users WHERE UserID = ?';

	connection.query(sql, userID, function(err,result){
		var resultCode = 404;
		var message = '에러 발생';

		if(err){
			console.log(err);
		}else{
			if(result.length ===0){
				resultCode = 204;
				message = '존재하지 않는 계정';
			}else if (userPwd !== result[0].UserPwd){
				resultCode = 204;
				message = '비밀번호 틀림';
			}else {
				resultCode = 200;
				message = '로그인 성공';
			}
        }

		res.json({
			'code':resultCode,
			'message':message
		});
	})
});





//모바일앱: 메뉴 키워드 검색
app.post('/menu/search',function(req,res){

    var storeid =req.body.storeID;
    var keyword = "%" + req.body.category + "%";
    console.log(storeid);
    console.log(keyword);
    var params=[keyword,storeid]
    var sql = 'SELECT menus.menu_name, menus.price, menus.description FROM menus, stores WHERE menu_name LIKE ? AND stores.beacon_ID = ? AND stores.store_num = menus.store_num';

	connection.query(sql, params, function(err,result){
        
        console.log(result);
        if(result.length ===0){
            console.log('키워드에 해당하는 메뉴 없음');
        }else{
           res.json({'array':result});
        }
	})
});

    



//모바일앱: 메뉴 기본재생
app.post('/menu/basicSearch',function(req,res){

	var becond_ID =  req.body.storeID ;
    var sql = 'SELECT stores.store_name, stores.beacon_ID, menus.menu_name, menus.price, menus.description, stores.stevt FROM menus, stores WHERE stores.beacon_ID = ? AND stores.store_num = menus.store_num';

	connection.query(sql, becond_ID, function(err,result){

        if(result.length ===0){
            console.log('기본재생 메뉴 없음');
        }else{
            console.log(result);
           res.json({'array':result});
        }
	})
});





//모바일앱: 메뉴 카테고리 검색
app.post('/menu/categorySearch',function(req,res){

	var becond_ID =  req.body.storeID ;
    var sql = 'SELECT distinct menus.category FROM menus, stores WHERE stores.beacon_ID = ? AND stores.store_num = menus.store_num';

	connection.query(sql, becond_ID, function(err,result){

        //if(result.length ===0){
        //    console.log('메뉴 카테고리를 찾을 수 없음');
        //}else{
        //   res.json({'array':result});
        //}

        console.log(result);
        res.json({'array':result});
	})
});





//모바일앱: 카테고리 선택 후 메뉴 반환
app.post('/menu/categoryResult',function(req,res){

    var beacon_ID = req.body.storeID;
    var category = req.body.category

	var params = [beacon_ID, category];
    var sql = 'SELECT menus.menu_name, menus.price, menus.description FROM menus, stores WHERE stores.beacon_ID = ? AND category=?';

	connection.query(sql, params, function(err,result){

        if(result.length ===0){
            console.log('카테고리에 해당하는 메뉴 검색결과 없음');
        }else{
            console.log(result);
            res.json({'array':result});
        }
	})
});





//모바일앱: 주문하기(고쳐야함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!)
app.post('/menu/order',function(req,res){

    var beacon_ID =  req.body.storeID ; //비콘 아이디
    var menu_order =  req.body.orderContent ; //음성으로 말한 주문 내역
    var user_id = req.body.userID; //주문한 사용자의 아이디
    var order_time = req.body.currentTime;

    var sql1 = 'SELECT store_num FROM stores WHERE beacon_ID = ?';
    var sql2 = 'INSERT into orders (store_num, user_id, order_time, menu_order) values(?,?,?,?)';

    connection.connect(function(err){ // 데이터베이스랑 연결
        if(err) throw err;
        
        connection.query(sql1, beacon_ID,function(err,result){

            if(err){
                console.log(err);
            }else{
                console.log('앱에서 받은 비콘 아이디로 store_num 검색 완료');
                var store_num = result[0].store_num;
                var params = [store_num, user_id, order_time, menu_order]

                //이중 DB connection 시작
                connection.query(sql2, params,function(err,result){

                    if(err){
                        console.log(err);
                    }else{
                        console.log('주문내역을 테이블에 저장 완료');

                        //const socket = ioclient("http://15.164.166.224:80");
                        //socket.emit("text",{mm:"ehrud"}); // 서버로 데이터 전송
                        
                        io.emit("order",{menu_order:menu_order});//클라이언트로 전송
                       
                        res.json({'array':'주문이 전달되었습니다'});
                    }
                });
		    }
        });
    });
});

