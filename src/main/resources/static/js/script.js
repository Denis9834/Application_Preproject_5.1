$(document).ready(function () {
    loadUsers();
    if ($('#user-info-role').length) {
        loadUserInfo();
    }
    loadCurrentUser();
    loadUserRolesSidebar();
    loadAllInterviews();
    loadUserInterviews()
});

//Загрузка пользователя в бар (отображение инфы о текущем пользователе)
function loadUserInfo() {
    $.ajax({
        url: '/api/user',
        method: 'GET',
        success: function (data) {
            $('#user-info-role').text("Пользователь " + data.name + " с ролью " +
                data.roles.map(r => r.role.replace("ROLE_", "")).join(' '));
        },
        error: function () {
            showMessage('error', 'Ошибка загрузки информации о пользователе');
        }
    });
}

//Отображение доступных ролей у пользователя в боковой панели (роли текущего пользователя)
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

//Обработка существующих ролей у пользователя и загрузка в боковую панель
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

// Загрузка списка пользователей у Admin
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

// Получение всех собеседований у Admin
function loadAllInterviews() {
    $.ajax({
        url: '/api/interviews/all',
        method: 'GET',
        dataType: 'json',
        success: function (data) {
            const tableBody = $('#interviewsAll tbody');
            tableBody.empty();
            data.forEach(interview => {
                const row = `
                <tr>
                        <td>${interview.userId}</td>
                        <td>${interview.userName}</td>
                        <td>${interview.organization}</td>
                        <td>${interview.grade}</td>
                        <td><a href="${interview.jobLink}" target="_blank" 
                        class="jobLinkPreview link-offset-2 link-underline link-underline-opacity-0 icon-link icon-link-hover" 
                                style="--bs-link-hover-color-rgb: 25, 135, 84;"
                                href="#">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" 
                                    class="bi bi-arrow-down-right-circle" viewBox="0 0 16 16">
                                    <path fill-rule="evenodd" d="M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8zm15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.854 5.146a.5.5 0 1 0-.708.708L9.243 9.95H6.475a.5.5 0 1 0 0 1h3.975a.5.5 0 0 0 .5-.5V6.475a.5.5 0 1 0-1 0v2.768L5.854 5.146z"/>
                                    <use xlink:href="#arrow-right">
                                    </svg>
                                Ссылка                                    
                            </a>
                        </td>
                        <td>${interview.contact}</td>
                        <td>${interview.project}</td>
                        <td>${new Date(interview.dataTime).toLocaleString()}</td>
                        <td>${interview.salaryOffer}</td>
                        <td>${interview.finalOffer == null ? "-" : interview.finalOffer}</td>
                        <td>${interview.interviewNotes == null ? "-" : interview.interviewNotes}</td>
                        <td>${interview.comments}</td>
                        <td>${interview.statusLabel}</td>
                        
                        <td>
                            ${interview.status === 'SCHEDULED' ?
                    `<button type="button" class="btn btn-info passed-interview-btn mb-2" 
                            data-id="${interview.id}">Прошел собеседование</button>` : ''}
                            ${interview.status === 'SCHEDULED' || interview.status === 'PASSED' ?
                    `<button class="btn btn-warning offer-received-btn mb-2" data-id="${interview.id}">Получен оффер</button>` : ""}
                            <button class="btn btn-success edit-user-interview mb-2" data-id="${interview.id}">Редактировать</button>
                            <button class="btn btn-danger delete-user-interview" data-id="${interview.id}">Удалить</button>
                        </td>
                    </tr>
                `;
                tableBody.append(row);
            });
        },
        error: function () {
            showMessage('error', 'Не удалось загрузить собеседования');
        }
    });
}

// Получение собеседований у User
function loadUserInterviews() {
    $.ajax({
        url: '/api/interviews',
        method: 'GET',
        dataType: 'json',
        success: function (interviews) {
            const tbody = $('#interviewsTable tbody');
            tbody.empty();
            interviews.forEach(interview => {
                const row = `
                    <tr>
                        <td>${interview.organization}</td>
                        <td>${interview.grade}</td>
                        <td><a href="${interview.jobLink}" target="_blank" 
                        class="jobLinkPreview link-offset-2 link-underline link-underline-opacity-0 icon-link icon-link-hover" 
                                style="--bs-link-hover-color-rgb: 25, 135, 84;"
                                href="#">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" 
                                    class="bi bi-arrow-down-right-circle" viewBox="0 0 16 16">
                                    <path fill-rule="evenodd" d="M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8zm15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.854 5.146a.5.5 0 1 0-.708.708L9.243 9.95H6.475a.5.5 0 1 0 0 1h3.975a.5.5 0 0 0 .5-.5V6.475a.5.5 0 1 0-1 0v2.768L5.854 5.146z"/>
                                    <use xlink:href="#arrow-right">
                                    </svg>
                                Ссылка                                    
                            </a>
                        </td>
                        <td>${interview.contact}</td>
                        <td>${interview.project}</td>
                        <td>${new Date(interview.dataTime).toLocaleString()}</td>
                        <td>${interview.salaryOffer}</td>
                        <td>${interview.finalOffer == null ? "-" : interview.finalOffer}</td>
                        <td>${interview.interviewNotes == null ? "-" : interview.interviewNotes}</td>
                        <td>${interview.comments}</td>
                        <td>${interview.statusLabel}</td>
                        
                        <td>
                            ${interview.status === 'SCHEDULED' ?
                    `<button type="button" class="btn btn-info passed-interview-btn mb-2" 
                            data-id="${interview.id}">Прошел собеседование</button>` : ''}
                            ${interview.status === 'SCHEDULED' || interview.status === 'PASSED' ?
                    `<button class="btn btn-warning offer-received-btn mb-2" data-id="${interview.id}">Получен оффер</button>` : ""}
                            <button class="btn btn-success edit-user-interview mb-2" data-id="${interview.id}">Редактировать</button>
                            <button class="btn btn-danger delete-user-interview" data-id="${interview.id}">Удалить</button>
                        </td>
                    </tr>
                `;
                tbody.append(row);
            });
        },
        error: function () {
            showMessage('error', 'Не удалось загрузить собеседования');
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

<!-- Обработчик Telegram -->
function onTelegramAuth(user) {
    fetch("/api/auth/telegram", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(user)
    }).then(res => {
        if (res.ok) {
            window.location.href = "/index";
        } else {
            alert("Ошибка авторизации через Telegram");
        }
    });
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

// Редактирование пользователя у User
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

// Сохранение изменений при редактировании пользователя (User)
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

// Удаление пользователя (модальное окно) (Admin)
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

// кнопка "Удалить" пользователя (Admin)
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

// Добавление нового пользователя (Admin)
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

// Показать модалку добавления собеседований общая (User/Admin)
$('#addInterview, #addUserInterview').on('click', function () {
    $('#addInterviewForm')[0].reset();
    const now = new Date();
    const formatted = now.toISOString().slice(0, 16);
    $('#interviewDataTime').val(formatted);
    $('#addInterviewModal').modal('show');
});

// Отправка формы добавления собеса
$('#addInterviewForm').on('submit', function (e) {
    e.preventDefault();

    const formData = {
        organization: $('input[name="organization"]').val(),
        grade: $('select[name="grade"]').val(),
        jobLink: $('input[name="jobLink"]').val(),
        contact: $('input[name="contact"]').val(),
        project: $('input[name="project"]').val(),
        dataTime: $('input[name="interviewDate"]').val(),
        salaryOffer: $('input[name="salaryOffer"]').val(),
        comments: $('input[name="comments"]').val()
    };

    $.ajax({
        url: '/api/interviews',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function () {
            $('#addInterviewModal').modal('hide');
            showMessage('success', 'Собеседование добавлено!');

            if ($('#adminPanel').hasClass('show active')) {
                loadAllInterviews(); // Админ
            } else {
                loadUserInterviews(); // Обычный пользователь
            }
        },
        error: function () {
            showMessage('error', 'Ошибка при добавлении собеседования');
        }
    });
});

// Общие переменные для update interview (User\Admin)
let currentStatus = null;

// Редактирование собеса (User\Admin)
$(document).on('click', '.edit-user-interview', function () {
    const id = $(this).data('id');
    $.get({
        url: `/api/interviews/${id}`,
        method: 'GET',
        success: function (interview) {
            $('#editInterviewId').val(interview.interviewId);
            $('#editOrganization').val(interview.organization);
            $('#editGrade').val(interview.grade);
            $('#editJobLink').val(interview.jobLink);
            $('#editContact').val(interview.contact);
            $('#editProject').val(interview.project);
            $('#editDate').val(interview.dataTime.slice(0, 16));
            $('#editSalaryOffer').val(interview.salaryOffer);
            $('#editFinalOffer').val(interview.finalOffer);
            $('#editComments').val(interview.comments);
            $('#editInterviewNotes').val(interview.interviewNotes);
            currentStatus = interview.status;
            $('#editInterviewModal').modal('show');
        },

        error: function () {
            showMessage('error', 'Ошибка при загрузке собеседования');
        }
    });
});

// Сохранение изменений после редактирования собеса (User\Admin)
$('#editInterviewForm').on('submit', function (e) {
    e.preventDefault();
    const id = $('#editInterviewId').val();
    const updatedData = {
        organization: $('#editOrganization').val(),
        grade: $('#editGrade').val(),
        jobLink: $('#editJobLink').val(),
        contact: $('#editContact').val(),
        project: $('#editProject').val(),
        dataTime: $('#editDate').val(),
        salaryOffer: $('#editSalaryOffer').val(),
        finalOffer: $('#editFinalOffer').val(),
        comments: $('#editComments').val(),
        interviewNotes: $('#editInterviewNotes').val(),
        status: currentStatus
    };

    $.ajax({
        url: `/api/interviews/${id}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(updatedData),
        success: function () {
            $('#editInterviewModal').modal('hide');
            showMessage('success', 'Запись обновлена');
            loadUserInterviews();
            loadAllInterviews()
        },
        error: function () {
            showMessage('error', 'Ошибка при обновлении записи');
        }
    });
});

// Общая переменная для хранения ID собеса, которое нужно удалить
let interviewToDeleteId = null;

//Удаление собеса (вызов модального окна) (User\Admin)
$(document).on('click', '.delete-user-interview', function () {
    // Сохраняем ID интервью, которое передаётся через data-id кнопки
    interviewToDeleteId = $(this).data('id');
    // Показываем модальное окно с ID "deleteInterviewModel"
    $('#deleteInterviewModel').modal('show');
});

// кнопка "Удалить" собеседование в модальном окне (User\Admin)
$('#confirmDeleteButtonInterview').on('click', function () {
    if (interviewToDeleteId != null) {
        $.ajax({
            url: `/api/interviews/${interviewToDeleteId}`,
            method: 'DELETE',
            success: function () {
                showMessage('success', 'Собеседование удалено');
                // Скрытие модального окна после успешного удаления
                $('#deleteInterviewModel').modal('hide');
                // Обновляем данные на странице
                loadUserInterviews();
                loadAllInterviews()
                // Обнуление переменной
                interviewToDeleteId = null;
            },
            error: function () {
                showMessage('error', 'Ошибка при удалении собеседования');
            }
        });
    }
});

// Обработчик для кнопки "Пройдено собеседование" (User\Admin)
$(document).on('click', '.passed-interview-btn', function () {
    var interviewId = $(this).data('id');
    $('#passedInterviewForm').data('interviewId', interviewId);
    $('#interviewNotes').val('');
    $('#passedInterviewModal').modal('show');
});

// Отправка формы для "Пройдено собеседование" (User\Admin)
$('#passedInterviewForm').on('submit', function (e) {
    e.preventDefault();
    var interviewId = $(this).data('interviewId');
    var notes = $('#interviewNotes').val();
    $.ajax({
        url: '/api/interviews/' + interviewId + '/passed',
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({notes: notes}),
        success: function (response) {
            $('#passedInterviewModal').modal('hide');
            loadUserInterviews();
            loadAllInterviews()
        },
        error: function () {
            alert('Ошибка при обновлении статуса собеседования');
        }
    });
});

// Обработчик для кнопки "Получен оффер" User\Admin
$(document).on('click', '.offer-received-btn', function () {
    var interviewId = $(this).data('id');
    $('#offerForm').data('interviewId', interviewId);
    $('offerAmount').val('');
    $('#offerModal').modal('show');
});

// Отправка формы для "Получен оффер" User\Admin
$('#offerForm').on('submit', function (e) {
    e.preventDefault();
    var interviewId = $(this).data('interviewId');
    var offer = $('#offerAmount').val();
    $.ajax({
        url: '/api/interviews/' + interviewId + '/offer',
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({offer: offer}),
        success: function (response) {
            $('#offerModal').modal('hide');
            loadUserInterviews();
            loadAllInterviews()
        },
        error: function () {
            alert('Ошибка при обновлении информации об оффере');
        }
    });
});

//Обработчик отображения предпросмотра по ссылке вакансии
$(document).on('mouseenter', '.jobLinkPreview', function (e) {
    const url = $(this).attr('href');
    previewTimeout = setTimeout(() => {
        $.get('/api/preview', {url: url}, function (data) {
            const previewHTML = `
                <strong>${data.title || 'Заголовок отсутствует'}</strong><br>
                ${data.image ? `<img src="${data.image}" alt="Картинка" style="width:100%; max-height:150px; object-fit:cover; margin:5px 0;">` : ''}
                <small>${data.description || 'Нет описания'}</small>
            `;
            $('#jobLinkPreviewBox')
                .html(previewHTML)
                .css({
                    top: e.pageY - $('#jobLinkPreviewBox').outerHeight() - 20,
                    left: e.pageX
                })
                .stop(true, true)
                .fadeIn(200);
        }).fail(() => {
            // Сообщение об ошибке
            const errorHTML = `
        <div>
            <strong class="text-danger">Превью недоступно</strong><br>
            Не удалось получить данные по ссылке
        </div>
    `;
            $('#jobLinkPreviewBox')
                .html(errorHTML)
                .css({
                    top: e.pageY - $('#jobLinkPreviewBox').outerHeight() - 20,
                    left: e.pageX
                })
                .stop(true, true)
                .fadeIn(200);
        });
    }, 300);
});

//Обработчик скрытия окна предпросмотра ссылки
$(document).on('mouseleave', '.jobLinkPreview', function () {
    clearTimeout(previewTimeout);
    $('#jobLinkPreviewBox').stop(true, true).fadeOut(200);
});

let userSearchTimeout;
//Поиск у User
$('#searchMyOrganization').on('input', function () {
    clearTimeout(userSearchTimeout);
    const term = $(this).val().trim();

    userSearchTimeout = setTimeout(() => {
        if (!term) {
            loadUserInterviews();  // Если строка пустая — покажем всё
        } else {
            $.getJSON('/api/interviews/search', {term})
                .done(function (data) {
                    const tbody = $('#interviewsTable tbody');
                    tbody.empty();
                    data.forEach(interview => {
                        const row = `
                        <tr>
                        <td>${interview.userId}</td>
                        <td>${interview.userName}</td>
                        <td>${interview.organization}</td>
                        <td>${interview.grade}</td>
                        <td><a href="${interview.jobLink}" target="_blank"
                        class="jobLinkPreview link-offset-2 link-underline link-underline-opacity-0 icon-link icon-link-hover"
                                style="--bs-link-hover-color-rgb: 25, 135, 84;"
                                href="#">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                                    class="bi bi-arrow-down-right-circle" viewBox="0 0 16 16">
                                    <path fill-rule="evenodd" d="M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8zm15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.854 5.146a.5.5 0 1 0-.708.708L9.243 9.95H6.475a.5.5 0 1 0 0 1h3.975a.5.5 0 0 0 .5-.5V6.475a.5.5 0 1 0-1 0v2.768L5.854 5.146z"/>
                                    <use xlink:href="#arrow-right">
                                    </svg>
                                Ссылка
                            </a>
                        </td>
                        <td>${interview.contact}</td>
                        <td>${interview.project}</td>
                        <td>${new Date(interview.dataTime).toLocaleString()}</td>
                        <td>${interview.salaryOffer}</td>
                        <td>${interview.finalOffer == null ? "-" : interview.finalOffer}</td>
                        <td>${interview.interviewNotes == null ? "-" : interview.interviewNotes}</td>
                        <td>${interview.comments}</td>
                        <td>${interview.statusLabel}</td>

                        <td>
                            ${interview.status === 'SCHEDULED' ?
                            `<button type="button" class="btn btn-info passed-interview-btn mb-2" 
                            data-id="${interview.id}">Прошел собеседование</button>` : ''}
                            ${interview.status === 'SCHEDULED' || interview.status === 'PASSED' ?
                            `<button class="btn btn-warning offer-received-btn mb-2" data-id="${interview.id}">Получен оффер</button>` : ""}
                            <button class="btn btn-success edit-user-interview mb-2" data-id="${interview.id}">Редактировать</button>
                            <button class="btn btn-danger delete-user-interview" data-id="${interview.id}">Удалить</button>
                        </td>
                        </tr>
                        `;
                        tbody.append(row);
                    });
                })
                .fail(() => showMessage('error', 'Ошибка поиска'));
        }
    }, 300); // 300 мс задержки после последнего нажатия
});

let adminSearchTimeout;
//Поиск у Admin
$('#searchAllOrganization').on('input', function () {
    clearTimeout(adminSearchTimeout);
    const term = $(this).val().trim();

    adminSearchTimeout = setTimeout(() => {
        if (!term) {
            loadAllInterviews();  // Если строка пустая — покажем всё
        } else {
            $.getJSON('/api/interviews/all/search', {term})
                .done(function (data) {
                    const tbody = $('#interviewsAll tbody');
                    tbody.empty();
                    data.forEach(interview => {
                        const row = `
                        <tr>
                        <td>${interview.userId}</td>
                        <td>${interview.userName}</td>
                        <td>${interview.organization}</td>
                        <td>${interview.grade}</td>
                        <td><a href="${interview.jobLink}" target="_blank"
                        class="jobLinkPreview link-offset-2 link-underline link-underline-opacity-0 icon-link icon-link-hover"
                                style="--bs-link-hover-color-rgb: 25, 135, 84;"
                                href="#">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                                    class="bi bi-arrow-down-right-circle" viewBox="0 0 16 16">
                                    <path fill-rule="evenodd" d="M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8zm15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.854 5.146a.5.5 0 1 0-.708.708L9.243 9.95H6.475a.5.5 0 1 0 0 1h3.975a.5.5 0 0 0 .5-.5V6.475a.5.5 0 1 0-1 0v2.768L5.854 5.146z"/>
                                    <use xlink:href="#arrow-right">
                                    </svg>
                                Ссылка
                            </a>
                        </td>
                        <td>${interview.contact}</td>
                        <td>${interview.project}</td>
                        <td>${new Date(interview.dataTime).toLocaleString()}</td>
                        <td>${interview.salaryOffer}</td>
                        <td>${interview.finalOffer == null ? "-" : interview.finalOffer}</td>
                        <td>${interview.interviewNotes == null ? "-" : interview.interviewNotes}</td>
                        <td>${interview.comments}</td>
                        <td>${interview.statusLabel}</td>

                        <td>
                            ${interview.status === 'SCHEDULED' ?
                            `<button type="button" class="btn btn-info passed-interview-btn mb-2" 
                            data-id="${interview.id}">Прошел собеседование</button>` : ''}
                            ${interview.status === 'SCHEDULED' || interview.status === 'PASSED' ?
                            `<button class="btn btn-warning offer-received-btn mb-2" data-id="${interview.id}">Получен оффер</button>` : ""}
                            <button class="btn btn-success edit-user-interview mb-2" data-id="${interview.id}">Редактировать</button>
                            <button class="btn btn-danger delete-user-interview" data-id="${interview.id}">Удалить</button>
                        </td>
                        </tr>
                        `;
                        tbody.append(row);
                    });
                })
                .fail(() => showMessage('error', 'Ошибка поиска'));
        }
    }, 300); // 300 мс задержки после последнего нажатия
});
