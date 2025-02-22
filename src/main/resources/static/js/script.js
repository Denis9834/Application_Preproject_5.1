$(document).ready(function () {
    loadUsers();
    loadUserInfo();
    loadCurrentUser();
    loadUserRolesSidebar()
});

//Загрузка пользователя в бар
function loadUserInfo() {
    $.ajax({
        url: '/api/user',
        method: 'GET',
        success: function (data) {
            $('#user-info-role').text(data.email + " с ролью " +
                data.roles.map(r => r.role.replace("ROLE_", "")).join(' '));
        },
        error: function () {
            showMessage('error', 'Ошибка загрузки информации о пользователе');
        }
    });
}

//Отображение доступных ролей у пользователя в боковой панели
function updateSidebar(userRoles) {
    const sidebar = $('#sidebar');
    sidebar.empty();

    const adminLink = $('<a href="#" id="admin-link" class="nav-link text-white">Admin</a>');
    const userLink = $('<a href="#" id="user-link" class="nav-link text-white">User</a>');

    const adminPanel = $('#adminPanel');
    const userPanel = $('#userPanel');

    adminPanel.hide().removeClass('show active');
    userPanel.hide().removeClass('show active');

    if (userRoles.includes('ROLE_ADMIN')) {
        sidebar.append(adminLink);
        sidebar.append(userLink);
        adminPanel.show().addClass('show active');
    } else if (userRoles.includes('ROLE_USER')) {
        sidebar.append(userLink);
        userPanel.show().addClass('show active');
    }

    adminLink.on('click', function (e) {
        e.preventDefault();
        userPanel.hide().removeClass('show active');
        adminPanel.show().addClass('show active');
    });

    userLink.on('click', function (e) {
        e.preventDefault();
        adminPanel.hide().removeClass('show active');
        userPanel.show().addClass('show active');
    });
}

function loadUserRolesSidebar() {
    $.ajax({
        url: '/api/user',
        method: 'GET',
        dataType: 'json',
        success: function (response) {
            if (response && response.roles) {
                let userRoles = response.roles.map(role => role.role ? role.role.trim() : null).filter(role => role !== null);
                updateSidebar(userRoles);
            } else {
                updateSidebar(['ROLE_USER']);
            }
        }
    });
}

//Загрузка страницы user "О пользователе"
function loadCurrentUser() {
    $.ajax({
        url: '/api/user',
        method: 'GET',
        dataType: 'json',
        success: function (user) {
            const tableBodyUser = $('#userTable-user tbody');
            tableBodyUser.empty();
            const roles = user.roles.map(r => r.role.replace("ROLE_", "")).join(' ');
            const row = `
                        <tr>
                            <td>${user.id}</td>
                            <td>${user.name}</td>
                            <td>${user.email}</td>
                            <td>${user.age}</td>
                           <td>${roles}</td>
                        </tr>
                    `;
            tableBodyUser.append(row);
        },
        error: function () {
            showMessage('error', 'Не удалось загрузить пользователей');
        }
    });
}

// Загрузка списка пользователей
function loadUsers() {
    $.ajax({
        url: '/api/admin/users',
        method: 'GET',
        dataType: 'json',
        success: function (data) {
            console.log("Полученные пользователи:", data);
            const tableBody = $('#usersTable-admin tbody');
            tableBody.empty();
            data.forEach(user => {
                const roles = user.roles.map(r => r.replace("ROLE_", "")).join(' ');
                const row = `
                        <tr>
                            <td>${user.id}</td>
                            <td>${user.name}</td>
                            <td>${user.email}</td>
                            <td>${user.age}</td>
                           <td>${roles}</td>
                            <td>
                                <button class="btn btn-success edit-btn" data-id="${user.id}">Редактировать</button>
                                <button class="btn btn-danger delete-btn" data-id="${user.id}">Удалить</button>
                            </td>
                        </tr>
                    `;
                tableBody.append(row);
            });
        },
        error: function () {
            showMessage('error', 'Не удалось загрузить пользователей');
        }
    });
}

// Отображение сообщений (ошибка/успех)
function showMessage(type, message) {
    const messageElement = type === 'error' ? $('#errorMessage') : $('#successMessage');
    messageElement.text(message).show();

    setTimeout(function () {
        messageElement.fadeOut(function () {
            messageElement.text('');
        });
    }, 5000);
}

// Кнопка выхода (Logout)
$('#logoutForm').on('submit', function (e) {
    e.preventDefault();
    $.post('/logout', function () {
        window.location.href = '/login';
    }).fail(function () {
        showMessage('error', 'Ошибка при выходе');
    });
});

// Редактирование пользователя
$(document).on('click', '.edit-btn', function () {
    const userId = $(this).data('id');
    $.ajax({
        url: `/api/admin/user/${userId}`,
        method: 'GET',
        success: function (user) {

            $('#editUserId').val(user.id);
            $('#editName').val(user.name);
            $('#editEmail').val(user.email);
            $('#editAge').val(user.age);

            $('#editForm-roles input[value="1"]').prop('checked', user.roles.some(r => r.role === 'ROLE_ADMIN'));
            $('#editForm-roles input[value="2"]').prop('checked', user.roles.some(r => r.role === 'ROLE_USER'));
            $('#modelEdit').modal('show');
        },
        error: function () {
            showMessage('error', 'Ошибка при загрузке данных пользователя');
        }
    });
});

// Сохранение изменений при редактировании пользователя
$('#editUserForm').on('submit', function (e) {
    e.preventDefault();

    const selectedRoles = [];
    $('#editForm-roles input:checked').each(function () {
        selectedRoles.push(parseInt($(this).val(), 10));
    });

    const updatedUser = {
        id: $('#editUserId').val(),
        name: $('#editName').val(),
        email: $('#editEmail').val(),
        age: parseInt($('#editAge').val(), 10),
        password: $('#editPassword').val(),
        roleId: selectedRoles
    };

    $.ajax({
        url: `/api/admin/user/${updatedUser.id}`,
        method: 'PUT',
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify(updatedUser),
        success: function () {
            showMessage('success', 'Пользователь успешно обновлен');
            $('#modelEdit').modal('hide');
            loadUsers();
        },
        error: function (xhr) {
            if (xhr.status === 400) {
                const errors = JSON.parse(xhr.responseText);
                $('#errorMessage').html("");
                for (const field in errors) {
                    $('#errorMessage').append(`<p class="text-danger">${errors[field]}</p>`);
                }
                $('#errorMessage').show();
            } else {
                showMessage('error', 'Ошибка при добавлении пользователя: ' + xhr.responseText);
            }
        }
    });
});

// Удаление пользователя (модальное окно)
$(document).on('click', '.delete-btn', function () {
    const userId = $(this).data('id');
    $.ajax({
        url: `/api/admin/user/${userId}`,
        method: 'GET',
        success: function (user) {
            $('#modalUserId').val(user.id);
            $('#modalName').val(user.name);
            $('#modalEmail').val(user.email);
            $('#modalAge').val(user.age);
            $('#modalRoles').val(user.roles.map(r => r.role.replace("ROLE_", "")).join(' '));
            $('#modelDelete').modal('show');
        },
        error: function () {
            showMessage('error', 'Ошибка при загрузке пользователя для удаления');
        }
    });
});

// кнопка "Удалить" пользователя
$('#confirmDeleteButton').on('click', function () {
    const userId = $('#modalUserId').val();
    $.ajax({
        url: `/api/admin/user/${userId}`,
        method: 'DELETE',
        success: function () {
            $('#modelDelete').modal('hide');
            loadUsers();
        },
        error: function () {
            showMessage('error', 'Ошибка при удалении пользователя');
        }
    });
});

// Добавление нового пользователя
$('#newUserForm').on('submit', function (e) {
    e.preventDefault();

    const selectedRoles = [];
    $('#newForm-roles input:checked').each(function () {
        selectedRoles.push(parseInt($(this).val(), 10));
    });

    const newUser = {
        name: $('#newName').val(),
        email: $('#newEmail').val(),
        age: parseInt($('#newAge').val(), 10),
        password: $('#newPassword').val(),
        roleId: selectedRoles
    };
    $.ajax({
        type: "POST",
        url: "/api/admin/users",
        contentType: "application/json; charset=UTF-8",
        data: JSON.stringify(newUser),
        success: function () {
            showMessage('success', 'Пользователь добавлен');
            // Закрытие модального окна
            $('#modelNew').modal('hide');
            // Очищаем форму модального окна
            $('#newUserForm')[0].reset();
            // Переключаемся на вкладку "Все пользователи"
            $('#users-tab').tab('show');
            // Обновляем таблицу пользователей
            loadUsers();
        },
        error: function (xhr) {
            if (xhr.status === 400) {
                const errors = JSON.parse(xhr.responseText);
                $('#errorMessage').html("");
                for (const field in errors) {
                    $('#errorMessage').append(`<p class="text-danger">${errors[field]}</p>`);
                }
                $('#errorMessage').show();
            } else {
                showMessage('error', 'Ошибка при добавлении пользователя: ' + xhr.responseText);
            }
        }
    });
});
